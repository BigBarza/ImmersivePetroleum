package flaxbeard.immersivepetroleum.common.items;

import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IColouredItem;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import net.minecraft.world.item.Item;

public class IPItemBase extends Item implements IColouredItem{
	/** For basic items */
	public IPItemBase(){
		this(new Item.Properties());
	}
	
	/** For items that require special attention */
	public IPItemBase(Item.Properties properties){
		super(properties.tab(ImmersivePetroleum.creativeTab));
	}
}
