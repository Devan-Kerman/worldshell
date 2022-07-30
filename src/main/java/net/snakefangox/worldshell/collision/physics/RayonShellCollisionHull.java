package net.snakefangox.worldshell.collision.physics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.math.Vector3f;
import dev.lazurite.rayon.api.EntityPhysicsElement;
import dev.lazurite.rayon.impl.bullet.collision.body.EntityRigidBody;
import net.snakefangox.worldshell.collision.ShellCollisionHull;
import net.snakefangox.worldshell.entity.WorldShellEntity;
import net.snakefangox.worldshell.math.Quaternion;
import net.snakefangox.worldshell.storage.Microcosm;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;

public class RayonShellCollisionHull extends ShellCollisionHull {
	public final EntityRigidBody body;
	
	public <T extends WorldShellEntity & EntityPhysicsElement> RayonShellCollisionHull(T entity) {
		super(entity);
		this.body = new EntityRigidBody(entity) {
			@Override
			public BuoyancyType getBuoyancyType() {
				return BuoyancyType.NONE;
			}
			
			@Override
			public DragType getDragType() {
				return DragType.NONE;
			}
		};
	}
	
	private static final Map<Vector3f, BoxCollisionShape> SHAPE_CACHE = new ConcurrentHashMap<>();
	
	@Override
	public void onWorldshellUpdate() {
		super.onWorldshellUpdate();
		this.onUpdate(body, entity);
	}
	
	@Override
	public void onWorldshellRotate() {
		super.onWorldshellRotate();
		Quaternion rotation = entity.getRotation();
		body.setPhysicsRotation(new com.jme3.math.Quaternion(
				(float) rotation.getX(),
				(float) rotation.getY(),
				(float) rotation.getZ(),
				(float) rotation.getW()
		));
	}
	
	@Override
	public Quaternion getRotation() {
		com.jme3.math.Quaternion orientation = new com.jme3.math.Quaternion();
		body.getPhysicsRotation(orientation);
		return new Quaternion(
				orientation.getX(),
				orientation.getY(),
				orientation.getZ(),
				orientation.getW()
		);
	}
	
	public void onUpdate(PhysicsCollisionObject object, WorldShellEntity entity) {
		Microcosm microcosm = entity.getMicrocosm();
		CompoundCollisionShape compoundShape = new CompoundCollisionShape();
		int bminX = Integer.MAX_VALUE, bminY = bminX, bminZ = bminX;
		for(Map.Entry<BlockPos, BlockState> block : microcosm.getBlocks()) {
			BlockPos key = block.getKey();
			bminX = Math.min(key.getX(), bminX);
			bminY = Math.min(key.getY(), bminY);
			bminZ = Math.min(key.getZ(), bminZ);
		}
		int fminX = bminX, fminY = bminY, fminZ = bminZ;
		Vector3f vec = new Vector3f();
		for(Map.Entry<BlockPos, BlockState> block : microcosm.getBlocks()) {
			BlockPos pos = block.getKey();
			BlockState value = block.getValue();
			VoxelShape shape = value.getCollisionShape(microcosm, pos);
			
			shape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
				vec.set((float) (maxX - minX), (float) (maxY - minY), (float) (maxZ - minZ));
				compoundShape.addChildShape(getOrCacheShape(vec),
						(float) minX + (pos.getX() - fminX),
						(float) minY + (pos.getY() - fminY),
						(float) minZ + (pos.getZ() - fminZ)
				);
			});
		}
		object.setCollisionShape(compoundShape);
	}
	
	public static BoxCollisionShape getOrCacheShape(Vector3f vec) {
		return SHAPE_CACHE.computeIfAbsent(vec, f -> {
			Vector3f newVec = f.clone();
			return new BoxCollisionShape(newVec);
		});
	}
}
