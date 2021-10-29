package dev.tr7zw.notenoughanimations.renderlayer;

import java.util.HashSet;
import java.util.Set;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import dev.tr7zw.notenoughanimations.NEAnimationsLoader;
import dev.tr7zw.notenoughanimations.access.PlayerData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class SwordRenderLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>{

    public SwordRenderLayer(
            RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderLayerParent) {
        super(renderLayerParent);
    }
    
    private boolean lazyInit = true;
    private static Set<Item> items = new HashSet<>();

    public static void update(Player player) {
        PlayerData data = (PlayerData) player;
        if(items.contains(player.getMainHandItem().getItem())) {
            data.setSideSword(player.getMainHandItem());
        }
        if(items.contains(player.getOffhandItem().getItem())) {
            data.setSideSword(player.getOffhandItem());
        }
    }
    
    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int light,
            AbstractClientPlayer player, float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4,
            float paramFloat5, float paramFloat6) {
        if(lazyInit) {
            lazyInit = false;
            init();
        }
        if(!NEAnimationsLoader.config.showLastUsedSword) {
            return;
        }
        if(!(player instanceof PlayerData)) {
            return;
        }
        if(player.isPassenger()) {
            return; // sitting player in a boat/minecart/on horse pokes the vehicle with the sword
        }
        PlayerData data = (PlayerData) player;
        ItemStack itemStack = data.getSideSword();
        if (itemStack.isEmpty())
            return;
        if(player.getMainHandItem() == itemStack || player.getOffhandItem() == itemStack) {
            return;
        }
        poseStack.pushPose();
        getParentModel().body.translateAndRotate(poseStack);
        boolean lefthanded = (player.getMainArm() == HumanoidArm.LEFT);
        boolean wearingArmor = !player.getItemBySlot(EquipmentSlot.CHEST).isEmpty() || !player.getItemBySlot(EquipmentSlot.LEGS).isEmpty();
        double offsetX = wearingArmor ? 0.3D : 0.28D;
        float swordRotation = -80F;
        if(lefthanded) {
            offsetX *= -1d;
        }
        poseStack.translate(offsetX, 0.85D, 0.25D);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(swordRotation));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
        
        Minecraft.getInstance().getItemInHandRenderer().renderItem(player, itemStack, lefthanded ? ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND : ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND, lefthanded,
                poseStack, multiBufferSource, light);
        poseStack.popPose();
    }
    
    private void init() {
        for(String itemKey : NEAnimationsLoader.config.sheathSwords) {
            if(itemKey.contains(":")) {
                Item item = Registry.ITEM.get(new ResourceLocation(itemKey.split(":")[0], itemKey.split(":")[1]));
                if(item != Items.AIR) {
                    items.add(item);
                }
            }
        }
    }

}
