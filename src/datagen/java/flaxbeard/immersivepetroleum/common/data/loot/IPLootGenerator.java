package flaxbeard.immersivepetroleum.common.data.loot;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.mojang.datafixers.util.Pair;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class IPLootGenerator extends LootTableProvider{
	public IPLootGenerator(DataGenerator pGenerator){
		super(pGenerator);
	}
	
	@Override
	protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables(){
		return List.of(
				Pair.of(IPLoot::new, LootContextParamSets.EMPTY),
				Pair.of(IPBlockLoot::new, LootContextParamSets.BLOCK)
			);
	}
	
	@Override
	protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationtracker){
		map.forEach((p_218436_2_, p_218436_3_) -> {
			LootTables.validate(validationtracker, p_218436_2_, p_218436_3_);
		});
	}
}
