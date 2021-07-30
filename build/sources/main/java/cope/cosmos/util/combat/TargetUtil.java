package cope.cosmos.util.combat;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.manager.managers.SocialManager.Relationship;
import cope.cosmos.client.features.modules.client.Social;
import cope.cosmos.util.Wrapper;
import cope.cosmos.util.world.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.util.*;
import java.util.stream.Collectors;

public class TargetUtil implements Wrapper {

    public static EntityPlayer getClosestPlayer(double range) {
        if (new ArrayList<>(mc.world.playerEntities).stream().noneMatch(entityPlayer -> entityPlayer != mc.player))
            return null;

        return new ArrayList<>(mc.world.playerEntities).stream().filter(entityPlayer -> !Cosmos.INSTANCE.getSocialManager().getSocial(entityPlayer.getName()).equals(Relationship.FRIEND) && Social.friends.getValue()).filter(entityPlayer -> mc.player != entityPlayer).filter(entityPlayer -> mc.player.getDistance(entityPlayer) <= range).filter(entityPlayer -> !entityPlayer.isDead).min(Comparator.comparing(entityPlayer -> mc.player.getDistance(entityPlayer))).orElse(null);
    }

    public static EntityPlayer getTargetPlayer(double range, Target target) {
        EntityPlayer targetPlayer = null;

        if (new ArrayList<>(mc.world.playerEntities).stream().filter(entityPlayer -> !Cosmos.INSTANCE.getSocialManager().getSocial(entityPlayer.getName()).equals(Relationship.FRIEND) && Social.friends.getValue()).filter(entityPlayer -> mc.player != entityPlayer).filter(entityPlayer -> mc.player.getDistance(entityPlayer) <= range).allMatch(EnemyUtil::isDead))
            return null;

        switch (target) {
            case CLOSEST:
                targetPlayer = new ArrayList<>(mc.world.playerEntities).stream().filter(entityPlayer -> !Cosmos.INSTANCE.getSocialManager().getSocial(entityPlayer.getName()).equals(Relationship.FRIEND) && Social.friends.getValue()).filter(entityPlayer -> mc.player != entityPlayer).filter(entityPlayer -> mc.player.getDistance(entityPlayer) <= range).filter(entityPlayer -> !EnemyUtil.isDead(entityPlayer)).findFirst().orElse(null);
                break;
            case LOWESTHEALTH:
                targetPlayer = new ArrayList<>(mc.world.playerEntities).stream().filter(entityPlayer -> !Cosmos.INSTANCE.getSocialManager().getSocial(entityPlayer.getName()).equals(Relationship.FRIEND) && Social.friends.getValue()).filter(entityPlayer -> mc.player != entityPlayer).filter(entityPlayer -> mc.player.getDistance(entityPlayer) <= range).filter(entityPlayer -> !EnemyUtil.isDead(entityPlayer)).min(Comparator.comparing(EnemyUtil::getHealth)).orElse(null);
                break;
            case LOWESTARMOR:
                targetPlayer = new ArrayList<>(mc.world.playerEntities).stream().filter(entityPlayer -> !Cosmos.INSTANCE.getSocialManager().getSocial(entityPlayer.getName()).equals(Relationship.FRIEND) && Social.friends.getValue()).filter(entityPlayer -> mc.player != entityPlayer).filter(entityPlayer -> mc.player.getDistance(entityPlayer) <= range).filter(entityPlayer -> !EnemyUtil.isDead(entityPlayer)).min(Comparator.comparing(EnemyUtil::getArmor)).orElse(null);
                break;
        }

        return targetPlayer;
    }

    public static Entity getTargetEntity(double range, Target target, boolean players, boolean passive, boolean neutral, boolean hostile) {
        EntityPlayer targetPlayer = null;

        if (new ArrayList<>(mc.world.loadedEntityList).stream().noneMatch(entityPlayer -> entityPlayer != mc.player))
            return null;

        switch (target) {
            case CLOSEST:
                targetPlayer = new ArrayList<>(mc.world.playerEntities).stream().filter(entityPlayer -> !Cosmos.INSTANCE.getSocialManager().getSocial(entityPlayer.getName()).equals(Relationship.FRIEND) && Social.friends.getValue()).filter(entityPlayer -> mc.player != entityPlayer).filter(entityPlayer -> mc.player.getDistance(entityPlayer) <= range).filter(entityPlayer -> !EnemyUtil.isDead(entityPlayer)).min(Comparator.comparing(entityPlayer -> mc.player.getDistance(entityPlayer))).orElse(null);
                break;
            case LOWESTHEALTH:
                targetPlayer = new ArrayList<>(mc.world.playerEntities).stream().filter(entityPlayer -> !Cosmos.INSTANCE.getSocialManager().getSocial(entityPlayer.getName()).equals(Relationship.FRIEND) && Social.friends.getValue()).filter(entityPlayer -> mc.player != entityPlayer).filter(entityPlayer -> mc.player.getDistance(entityPlayer) <= range).filter(entityPlayer -> !EnemyUtil.isDead(entityPlayer)).min(Comparator.comparing(EnemyUtil::getHealth)).orElse(null);
                break;
            case LOWESTARMOR:
                targetPlayer = new ArrayList<>(mc.world.playerEntities).stream().filter(entityPlayer -> !Cosmos.INSTANCE.getSocialManager().getSocial(entityPlayer.getName()).equals(Relationship.FRIEND) && Social.friends.getValue()).filter(entityPlayer -> mc.player != entityPlayer).filter(entityPlayer -> mc.player.getDistance(entityPlayer) <= range).filter(entityPlayer -> !EnemyUtil.isDead(entityPlayer)).min(Comparator.comparing(EnemyUtil::getArmor)).orElse(null);
                break;
        }

        if (players && targetPlayer != null)
            return targetPlayer;
        else {
            List<Entity> verifiedEntities = new ArrayList<>();

            for (Entity entity : new ArrayList<>(mc.world.loadedEntityList).stream().filter(entity -> entity != mc.player).collect(Collectors.toList())) {
                if (hostile && EntityUtil.isHostileMob(entity))
                    verifiedEntities.add(entity);
                else if (neutral && EntityUtil.isNeutralMob(entity))
                    verifiedEntities.add(entity);
                else if (passive && EntityUtil.isPassiveMob(entity))
                    verifiedEntities.add(entity);
            }

            return verifiedEntities.stream().min(Comparator.comparing(entity -> mc.player.getDistance(entity))).orElse(null);
        }
    }

    public enum Target {
        CLOSEST, LOWESTHEALTH, LOWESTARMOR
    }
}