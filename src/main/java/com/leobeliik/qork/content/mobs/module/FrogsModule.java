package com.leobeliik.qork.content.mobs.module;

import com.leobeliik.qork.base.Qork;
import com.leobeliik.qork.content.mobs.client.render.entity.FroggeRenderer;
import com.leobeliik.qork.content.mobs.entity.Frogge;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import org.violetmoon.zeta.client.event.load.ZClientSetup;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.config.type.CompoundBiomeConfig;
import org.violetmoon.zeta.config.type.EntitySpawnConfig;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZEntityAttributeCreation;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;

@ZetaLoadModule(category = "qork-mobs")
public class FrogsModule extends ZetaModule {

    public static EntityType<Frogge> frogType;

    @Config
    public static EntitySpawnConfig spawnConfig = new EntitySpawnConfig(40, 1, 3, CompoundBiomeConfig.fromBiomeTags(false, BiomeTags.IS_END));

    @Config
    public static boolean enableBigFunny = false;

    public static Item frogLeg;
    public static Item cookedFrogLeg;

    @LoadEvent
    public void register(ZRegister event) {
       /* frogLeg = new FroggeLegItem("frog_leg", this, new Item.Properties()
                .tab(CreativeModeTab.TAB_FOOD)
                .food(new FoodProperties.Builder()
                        .meat()
                        .nutrition(2)
                        .saturationMod(0.3F)
                        .build()));

        cookedFrogLeg = new FrogLegItem("cooked_frog_leg", this, new Item.Properties()
                .tab(CreativeModeTab.TAB_FOOD)
                .food(new FoodProperties.Builder()
                        .meat()
                        .nutrition(4)
                        .saturationMod(1.25F)
                        .build()));

        Item goldenLeg = new FrogLegItem("golden_frog_leg", this, new Item.Properties()
                .tab(CreativeModeTab.TAB_BREWING)
                .food(new FoodProperties.Builder()
                        .meat()
                        .nutrition(4)
                        .saturationMod(2.5F)
                        .build()))
                .setCondition(() -> enableBrewing);

        BrewingHandler.addPotionMix("frog_brewing",
                () -> new FlagIngredient(Ingredient.of(goldenLeg), "frogs"),
                Potions.LEAPING, Potions.LONG_LEAPING, Potions.STRONG_LEAPING);*/

        frogType = EntityType.Builder.<Frogge>of(Frogge::new, MobCategory.CREATURE)
                .sized(0.65F, 0.5F)
                .clientTrackingRange(8)
                .setCustomClientFactory((spawnEntity, world) -> new Frogge(frogType, world))
                .build("frogge");
        Qork.QORK_ZETA.registry.register(frogType, "frogge", Registries.ENTITY_TYPE);
        Qork.QORK_ZETA.entitySpawn.addEgg(this, frogType, 0xbc9869, 0xffe6ad, spawnConfig);
    }

    @LoadEvent
    public final void entityAttrs(ZEntityAttributeCreation e) {
        e.put(frogType, Frogge.prepareAttributes().build());
    }

    @LoadEvent
    public final void clientSetup(ZClientSetup event) {
        EntityRenderers.register(frogType, FroggeRenderer::new);
    }


}
