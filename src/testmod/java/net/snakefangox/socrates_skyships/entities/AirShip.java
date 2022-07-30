package net.snakefangox.socrates_skyships.entities;

import dev.lazurite.rayon.api.EntityPhysicsElement;
import dev.lazurite.rayon.impl.bullet.collision.body.EntityRigidBody;
import net.snakefangox.socrates_skyships.SRegister;
import net.snakefangox.worldshell.collision.ShellCollisionHull;
import net.snakefangox.worldshell.collision.physics.RayonShellCollisionHull;
import net.snakefangox.worldshell.entity.WorldShellEntity;
import net.snakefangox.worldshell.math.Quaternion;

import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public class AirShip extends WorldShellEntity implements EntityPhysicsElement {
	public AirShip(EntityType<?> type, World world) {
		super(type, world, SRegister.AIRSHIP_SETTINGS);
	}
	
	@Override
	public EntityRigidBody getRigidBody() {
		return ((RayonShellCollisionHull)this.hull).body;
	}
	
	@Override
	protected ShellCollisionHull createHull(boolean handleRotation) {
		return new RayonShellCollisionHull(this);
	}
	
	@Override
	public Quaternion getRotation() {
		return hull.getRotation();
	}
}
