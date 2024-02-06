package com.leobeliik.qork.base.client.handler;

import com.leobeliik.qork.content.mobs.client.model.FroggeModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.violetmoon.zeta.client.event.load.ZRegisterLayerDefinitions;
import org.violetmoon.zeta.event.bus.LoadEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.leobeliik.qork.base.Qork.MODID;

public class ModelHandler {
    private static final Map<ModelLayerLocation, Layer> layers = new HashMap<ModelLayerLocation, Layer>();
    public static ModelLayerLocation frogge;

    private static boolean modelsInitted = false;

    private static void initModels() {
        if (modelsInitted)
            return;

        frogge = addModel("frogge", FroggeModel::createBodyLayer, FroggeModel::new);

        modelsInitted = true;
    }

    private static ModelLayerLocation addModel(String name, Supplier<LayerDefinition> supplier, Function<ModelPart, EntityModel<?>> modelConstructor) {
        return addLayer(name, new Layer(supplier, modelConstructor));
    }

    /*private static ModelLayerLocation addArmorModel(String name, Supplier<LayerDefinition> supplier) {
        return addLayer(name, new Layer(supplier, QuarkArmorModel::new));
    }*/

    @OnlyIn(Dist.CLIENT)
    private static ModelLayerLocation addLayer(String name, Layer layer) {
        ModelLayerLocation loc = new ModelLayerLocation(new ResourceLocation(MODID, name), "main");
        layers.put(loc, layer);
        return loc;
    }

    @LoadEvent
    public static void registerLayer(ZRegisterLayerDefinitions event) {
        initModels();

        for (ModelLayerLocation location : layers.keySet()) {
            //Quark.LOG.info("Registering model layer " + location);
            event.registerLayerDefinition(location, layers.get(location).definition);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Mob, M extends EntityModel<T>> M model(ModelLayerLocation location) {
        initModels();

        Layer layer = layers.get(location);
        Minecraft mc = Minecraft.getInstance();

        return (M) layer.modelConstructor.apply(mc.getEntityModels().bakeLayer(location));
    }

   /* public static QuarkArmorModel armorModel(ModelLayerLocation location, EquipmentSlot slot) {
        Pair<ModelLayerLocation, EquipmentSlot> key = Pair.of(location, slot);
        if (cachedArmors.containsKey(key))
            return cachedArmors.get(key);

        initModels();

        Layer layer = layers.get(location);
        Minecraft mc = Minecraft.getInstance();
        QuarkArmorModel model = layer.armorModelConstructor.apply(mc.getEntityModels().bakeLayer(location), slot);
        cachedArmors.put(key, model);

        return model;
    }*/

    private static class Layer {

        final Supplier<LayerDefinition> definition;
        final Function<ModelPart, EntityModel<?>> modelConstructor;
        //final BiFunction<ModelPart, EquipmentSlot, QuarkArmorModel> armorModelConstructor;

        public Layer(Supplier<LayerDefinition> definition, Function<ModelPart, EntityModel<?>> modelConstructor) {
            this.definition = definition;
            this.modelConstructor = modelConstructor;
            //this.armorModelConstructor = null;
        }

        /*public Layer(Supplier<LayerDefinition> definition, BiFunction<ModelPart, EquipmentSlot, QuarkArmorModel> armorModelConstructor) {
            this.definition = definition;
            this.modelConstructor = null;
            //this.armorModelConstructor = armorModelConstructor;
        }*/

    }
}
