package flaxbeard.immersivepetroleum.client;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Quaternion;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.ItemOverlayUtils;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.stone.CoresampleBlockEntity;
import blusunrize.immersiveengineering.common.items.BuzzsawItem;
import blusunrize.immersiveengineering.common.items.ChemthrowerItem;
import blusunrize.immersiveengineering.common.items.CoresampleItem;
import blusunrize.immersiveengineering.common.items.DrillItem;
import blusunrize.immersiveengineering.common.items.IEShieldItem;
import blusunrize.immersiveengineering.common.items.RailgunItem;
import blusunrize.immersiveengineering.common.items.RevolverItem;
import blusunrize.immersiveengineering.common.items.SpeedloaderItem;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.ILubricationHandler;
import flaxbeard.immersivepetroleum.api.crafting.reservoir.Reservoir;
import flaxbeard.immersivepetroleum.api.crafting.reservoir.ReservoirHandler;
import flaxbeard.immersivepetroleum.common.CommonEventHandler;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.blocks.wooden.AutoLubricatorBlock;
import flaxbeard.immersivepetroleum.common.entity.MotorboatEntity;
import flaxbeard.immersivepetroleum.common.items.DebugItem;
import flaxbeard.immersivepetroleum.common.util.MCUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent.OverlayType;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;

public class ClientEventHandler{
	
	@SubscribeEvent
	public void renderLast(RenderLevelLastEvent event){
		PoseStack matrix = event.getPoseStack();
		Minecraft mc = Minecraft.getInstance();
		
		/*
		if(IPClientConfig.MISCELLANEOUS.sample_displayBorder.get() && mc.player != null){
			PlayerEntity player = mc.player;
			
			transform.push();
			{
				boolean chunkBorders = false;
				for(Hand hand:Hand.values()){
					if(player.getHeldItem(hand).getItem().equals(IEBlocks.MetalDevices.sampleDrill.asItem())){
						chunkBorders = true;
						break;
					}
				}
				
				if(!chunkBorders && Minecraft.getInstance().objectMouseOver != null && Minecraft.getInstance().objectMouseOver.getType() == Type.BLOCK && mc.world.getTileEntity(new BlockPos(mc.objectMouseOver.getHitVec())) instanceof SampleDrillTileEntity)
					chunkBorders = true;
				
				ItemStack mainItem = player.getHeldItemMainhand();
				ItemStack secondItem = player.getHeldItemOffhand();
				boolean main = !mainItem.isEmpty() && mainItem.getItem() instanceof CoresampleItem;
				boolean off = !secondItem.isEmpty() && secondItem.getItem() instanceof CoresampleItem;
				
				ItemStack target = main ? mainItem : secondItem;
				if(!chunkBorders && (main || off)){
					ColumnPos pos=CoresampleItem.getCoords(target);
					if(pos!=null){
						//renderChunkBorder(transform, pos.x >> 4 << 4, pos.z >> 4 << 4);
					}
				}
			}
			transform.pop();
		}
		*/
		
		matrix.pushPose();
		{
			if(mc.player != null){
				ItemStack mainItem = mc.player.getMainHandItem();
				ItemStack secondItem = mc.player.getOffhandItem();
				
				boolean main = (mainItem != null && !mainItem.isEmpty()) && mainItem.getItem() == IPContent.Blocks.AUTO_LUBRICATOR.get().asItem();
				boolean off = (secondItem != null && !secondItem.isEmpty()) && secondItem.getItem() == IPContent.Blocks.AUTO_LUBRICATOR.get().asItem();
				
				if(main || off){
					BlockRenderDispatcher blockDispatcher = Minecraft.getInstance().getBlockRenderer();
					MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
					
					// Anti-Jiggle when moving
					Vec3 renderView = MCUtil.getGameRenderer().getMainCamera().getPosition();
					matrix.translate(-renderView.x, -renderView.y, -renderView.z);
					
					BlockPos base = mc.player.blockPosition();
					for(int x = -16;x <= 16;x++){
						for(int z = -16;z <= 16;z++){
							for(int y = -16;y <= 16;y++){
								BlockPos pos = base.offset(x, y, z);
								BlockEntity te = mc.player.level.getBlockEntity(pos);
								
								if(te != null){
									ILubricationHandler<BlockEntity> handler = (ILubricationHandler<BlockEntity>) LubricatedHandler.getHandlerForTile(te);
									if(handler != null){
										Tuple<BlockPos, Direction> target = handler.getGhostBlockPosition(mc.player.level, te);
										if(target != null){
											BlockPos targetPos = target.getA();
											Direction targetFacing = target.getB();
											BlockState targetState = mc.player.level.getBlockState(targetPos);
											BlockState targetStateUp = mc.player.level.getBlockState(targetPos.above());
											if(targetState.getMaterial().isReplaceable() && targetStateUp.getMaterial().isReplaceable()){
												VertexConsumer vBuilder = buffer.getBuffer(RenderType.translucent());
												matrix.pushPose();
												{
													matrix.translate(targetPos.getX(), targetPos.getY() - 1, targetPos.getZ());
													
													BlockState state = IPContent.Blocks.AUTO_LUBRICATOR.get().defaultBlockState().setValue(AutoLubricatorBlock.FACING, targetFacing);
													BakedModel model = blockDispatcher.getBlockModel(state);
													blockDispatcher.getModelRenderer().renderModel(matrix.last(), vBuilder, null, model, 1.0F, 1.0F, 1.0F, 0xF000F0, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
													
												}
												matrix.popPose();
												
												//IPShaders.alpha_static(0.5f, mc.player.tickCount);
												buffer.endBatch();
												//IPShaders.releaseShader();
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		matrix.popPose();
	}
	
	public void renderChunkBorder(PoseStack matrix, int chunkX, int chunkZ){
		Player player = MCUtil.getPlayer();
		
		double px = player.getX();
		double py = player.getY();
		double pz = player.getZ();
		int y = Math.min((int) py - 2, player.getCommandSenderWorld().getChunk(chunkX, chunkZ).getMaxBuildHeight());
		float h = (float) Math.max(32, py - y + 4);
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder vertexbuffer = tessellator.getBuilder();
		
		float r = Lib.COLOUR_F_ImmersiveOrange[0];
		float g = Lib.COLOUR_F_ImmersiveOrange[1];
		float b = Lib.COLOUR_F_ImmersiveOrange[2];
		matrix.translate(chunkX - px, y + 2 - py, chunkZ - pz);
		// transform.lineWidth(5f);
		vertexbuffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
//		vertexbuffer.begin(GL11.GL_LINES, DefaultVertexFormat.POSITION_COLOR);
		vertexbuffer.vertex(0, 0, 0).color(r, g, b, .375f).endVertex();
		vertexbuffer.vertex(0, h, 0).color(r, g, b, .375f).endVertex();
		vertexbuffer.vertex(16, 0, 0).color(r, g, b, .375f).endVertex();
		vertexbuffer.vertex(16, h, 0).color(r, g, b, .375f).endVertex();
		vertexbuffer.vertex(16, 0, 16).color(r, g, b, .375f).endVertex();
		vertexbuffer.vertex(16, h, 16).color(r, g, b, .375f).endVertex();
		vertexbuffer.vertex(0, 0, 16).color(r, g, b, .375f).endVertex();
		vertexbuffer.vertex(0, h, 16).color(r, g, b, .375f).endVertex();
		
		vertexbuffer.vertex(0, 2, 0).color(r, g, b, .375f).endVertex();
		vertexbuffer.vertex(16, 2, 0).color(r, g, b, .375f).endVertex();
		vertexbuffer.vertex(0, 2, 0).color(r, g, b, .375f).endVertex();
		vertexbuffer.vertex(0, 2, 16).color(r, g, b, .375f).endVertex();
		vertexbuffer.vertex(0, 2, 16).color(r, g, b, .375f).endVertex();
		vertexbuffer.vertex(16, 2, 16).color(r, g, b, .375f).endVertex();
		vertexbuffer.vertex(16, 2, 0).color(r, g, b, .375f).endVertex();
		vertexbuffer.vertex(16, 2, 16).color(r, g, b, .375f).endVertex();
		tessellator.end();
	}
	
	static final DecimalFormat FORMATTER = new DecimalFormat("#,###.##");
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void handleItemTooltip(ItemTooltipEvent event){
		ItemStack stack = event.getItemStack();
		if(stack.getItem() instanceof CoresampleItem){
			if(ItemNBTHelper.hasKey(stack, "resAmount")){
				String resName = ItemNBTHelper.hasKey(stack, "resType") ? ItemNBTHelper.getString(stack, "resType") : null;
				if(ItemNBTHelper.hasKey(stack, "resAmount") && resName == null){
					resName = "";
				}
				
				Reservoir reservoir = null;
				for(Reservoir res:Reservoir.map.values()){
					if(resName.equalsIgnoreCase(res.name)){
						reservoir = res;
					}
				}
				
				List<Component> tooltip = event.getToolTip();
				int amnt = ItemNBTHelper.getInt(stack, "resAmount");
				int tipPos = Math.max(0, tooltip.size() - 5);
				
				if(reservoir != null && amnt > 0){
					int est = (amnt / 1000) * 1000;
					Component fluidName = new FluidStack(reservoir.getFluid(), 1).getDisplayName();
					
					Component header = new TranslatableComponent("chat.immersivepetroleum.info.coresample.oil", fluidName).withStyle(ChatFormatting.GRAY);
					
					Component info = new TextComponent("  " + FORMATTER.format(est) + " mB").withStyle(ChatFormatting.DARK_GRAY);
					
					tooltip.add(tipPos, header);
					tooltip.add(tipPos + 1, info);
				}else{
					if(reservoir != null && reservoir.residual > 0){
						String fluidName = new FluidStack(reservoir.getFluid(), 1).getDisplayName().getContents();
						
						Component header = new TranslatableComponent("chat.immersivepetroleum.info.coresample.oil", fluidName).withStyle(ChatFormatting.GRAY);
						
						Component info = new TextComponent("  " + I18n.get("chat.immersivepetroleum.info.coresample.oilRep", reservoir.residual, fluidName)).withStyle(ChatFormatting.GRAY);
						
						tooltip.add(tipPos, header);
						tooltip.add(tipPos + 1, info);
					}else{
						tooltip.add(tipPos, new TextComponent(I18n.get("chat.immersivepetroleum.info.coresample.noOil")).withStyle(ChatFormatting.GRAY));
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void reservoirDebuggingOverlayText(RenderGameOverlayEvent.Post event){
		if(ReservoirHandler.generator == null){
			return;
		}
		
		Player player = MCUtil.getPlayer();
		
		ItemStack main = player.getItemInHand(InteractionHand.MAIN_HAND);
		ItemStack off = player.getItemInHand(InteractionHand.OFF_HAND);
		
		if((main != ItemStack.EMPTY && main.getItem() == IPContent.DEBUGITEM.get()) || (off != ItemStack.EMPTY && off.getItem() == IPContent.DEBUGITEM.get())){
			if(!((main != null && DebugItem.getMode(main) == DebugItem.Modes.SEEDBASED_RESERVOIR) || (off != null && DebugItem.getMode(off) == DebugItem.Modes.SEEDBASED_RESERVOIR))){
				return;
			}
			
			List<Component> debugOut = new ArrayList<>();
			
			if(!debugOut.isEmpty()){
				PoseStack matrix = event.getMatrixStack();
				matrix.pushPose();
				MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
				for(int i = 0;i < debugOut.size();i++){
					int w = ClientUtils.font().width(debugOut.get(i).getString());
					int yOff = i * (ClientUtils.font().lineHeight + 2);
					
					matrix.pushPose();
					matrix.translate(0, 0, 1);
					GuiHelper.drawColouredRect(1, 1 + yOff, w+1, 10, 0xAF_000000, buffer, matrix);
					buffer.endBatch();
					// Draw string without shadow
					ClientUtils.font().draw(matrix, debugOut.get(i), 2, 2 + yOff, -1);
					matrix.popPose();
				}
				matrix.popPose();
			}
		}
	}
	
	@SubscribeEvent
	public void renderInfoOverlays(RenderGameOverlayEvent.Post event){
		if(MCUtil.getPlayer() != null && event.getType() == RenderGameOverlayEvent.ElementType.TEXT){
			Player player = MCUtil.getPlayer();
			
			if(MCUtil.getHitResult() != null){
				boolean hammer = player.getItemInHand(InteractionHand.MAIN_HAND) != null && Utils.isHammer(player.getItemInHand(InteractionHand.MAIN_HAND));
				HitResult mop = MCUtil.getHitResult();
				
				if(mop != null){
					switch(mop.getType()){
						case BLOCK:{
							if(mop instanceof BlockHitResult){
								BlockEntity tileEntity = player.level.getBlockEntity(((BlockHitResult) mop).getBlockPos());
								
								if(tileEntity instanceof CoresampleBlockEntity){
									IBlockOverlayText overlayBlock = (IBlockOverlayText) tileEntity;
									Component[] text = overlayBlock.getOverlayText(player, mop, hammer);
									ItemStack coresample = ((CoresampleBlockEntity) tileEntity).coresample;
									
									if(ItemNBTHelper.hasKey(coresample, "resAmount") && text != null && text.length > 0){
										String resName = ItemNBTHelper.hasKey(coresample, "resType") ? ItemNBTHelper.getString(coresample, "resType") : "";
										int amnt = ItemNBTHelper.getInt(coresample, "resAmount");
										int i = text.length;
										
										Reservoir reservoir = null;
										for(Reservoir res:Reservoir.map.values()){
											if(resName.equals(res.name)){
												reservoir = res;
											}
										}
										
										// LanguageMap.getInstance().getVisualOrder(fluidName)
										
										Component display = new TranslatableComponent("chat.immersivepetroleum.info.coresample.noOil");
										
										if(reservoir != null && amnt > 0){
											Component fluidName = new FluidStack(reservoir.getFluid(), 1).getDisplayName();
											display = new TranslatableComponent("chat.immersivepetroleum.info.coresample.oil", fluidName);
										}else if(reservoir != null && reservoir.residual > 0){
											Component fluidName = new FluidStack(reservoir.getFluid(), 1).getDisplayName();
											display = new TranslatableComponent("chat.immersivepetroleum.info.coresample.oilRep", reservoir.residual, fluidName);
										}
										
										int fx = event.getWindow().getGuiScaledWidth() / 2 + 8;
										int fy = event.getWindow().getGuiScaledHeight() / 2 + 8 + i * ClientUtils.font().lineHeight;
										
										MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
										ClientUtils.font().drawInBatch(Language.getInstance().getVisualOrder(display), fx, fy, 0xFFFFFFFF, true, event.getMatrixStack().last().pose(), buffer, false, 0, 0xF000F0);
										buffer.endBatch();
									}
								}
							}
							break;
						}
						case ENTITY:{
							EntityHitResult rtr = (EntityHitResult) mop;
							if(rtr.getEntity() instanceof MotorboatEntity){
								String[] text = ((MotorboatEntity) rtr.getEntity()).getOverlayText(player, mop);
								
								if(text != null && text.length > 0){
									Font font = ClientUtils.font();
									int col = 0xffffff;
									for(int i = 0;i < text.length;i++){
										if(text[i] != null){
											int fx = event.getWindow().getGuiScaledWidth() / 2 + 8;
											int fy = event.getWindow().getGuiScaledHeight() / 2 + 8 + i * font.lineHeight;
											font.drawShadow(event.getMatrixStack(), text[i], fx, fy, col);
										}
									}
								}
							}
							break;
						}
						default:
							break;
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onRenderOverlayPost(RenderGameOverlayEvent.Post event){
		if(MCUtil.getPlayer() != null && event.getType() == RenderGameOverlayEvent.ElementType.TEXT){
			Player player = MCUtil.getPlayer();
			PoseStack matrix = event.getMatrixStack();
			
			if(player.getVehicle() instanceof MotorboatEntity){
				int offset = 0;
				boolean holdingDebugItem = false;
				for(InteractionHand hand:InteractionHand.values()){
					if(!player.getItemInHand(hand).isEmpty()){
						ItemStack equipped = player.getItemInHand(hand);
						if((equipped.getItem() instanceof DrillItem) || (equipped.getItem() instanceof ChemthrowerItem) || (equipped.getItem() instanceof BuzzsawItem)){
							offset -= 85;
						}else if((equipped.getItem() instanceof RevolverItem) || (equipped.getItem() instanceof SpeedloaderItem)){
							offset -= 65;
						}else if(equipped.getItem() instanceof RailgunItem){
							offset -= 50;
						}else if(equipped.getItem() instanceof IEShieldItem){
							offset -= 40;
						}
						
						if(equipped.getItem() instanceof DebugItem){
							holdingDebugItem = true;
						}
					}
				}
				
				matrix.pushPose();
				{
					int scaledWidth = MCUtil.getWindow().getGuiScaledWidth();
					int scaledHeight = MCUtil.getWindow().getGuiScaledHeight();
					
					MotorboatEntity boat = (MotorboatEntity) player.getVehicle();
					MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
					VertexConsumer builder = ItemOverlayUtils.getHudElementsBuilder(buffer);
					
					int rightOffset = 0;
					if(MCUtil.getOptions().showSubtitles)
						rightOffset += 100;
					float dx = scaledWidth - rightOffset - 16;
					float dy = scaledHeight + offset;
					matrix.pushPose();
					{
						matrix.translate(dx, dy, 0);
						GuiHelper.drawTexturedRect(builder, matrix, -24, -68, 31, 62, 256f, 179, 210, 9, 71);
						
						matrix.translate(-23, -37, 0);
						float capacity = boat.getMaxFuel();
						if(capacity > 0){
							FluidStack fuel = boat.getContainedFluid();
							int amount = fuel.getAmount();
							float angle = 83 - (166 * amount / capacity);
							matrix.pushPose();
							matrix.mulPose(new Quaternion(0, 0, angle, true));
							GuiHelper.drawTexturedRect(builder, matrix, 6, -2, 24, 4, 256f, 91, 123, 80, 87);
							matrix.popPose();
							matrix.translate(23, 37, 0);
							
							GuiHelper.drawTexturedRect(builder, matrix, -41, -73, 53, 72, 256f, 8, 61, 4, 76);
						}
					}
					matrix.popPose();
					
					buffer.endBatch();
					
					if(holdingDebugItem && MCUtil.getFont() != null){
						matrix.pushPose();
						{
							Font font = MCUtil.getFont();
							
							int capacity = boat.getMaxFuel();
							FluidStack fs = boat.getContainedFluid();
							int amount = (fs == FluidStack.EMPTY || fs.getFluid() == null) ? 0 : fs.getAmount();
							
							Vec3 vec = boat.getDeltaMovement();
							float speed = (float) Math.sqrt(vec.x * vec.x + vec.z * vec.z);
							
							String[] array = {
									String.format(Locale.US, "Fuel: %05d/%d mB (%s)", amount, capacity, fs.getDisplayName().getString()),
									String.format(Locale.US, "Speed: %.3f", speed),
									String.format(Locale.US, "PropXRot: %07.3f° (%.3frad)", boat.propellerXRot, boat.propellerXRot * Mth.DEG_TO_RAD),
									String.format(Locale.US, "PropSpeed: %06.3f°", boat.propellerXRotSpeed),
							};
							int w = 3, h = 3;
							for(int i = 0;i < array.length;i++){
								font.drawShadow(matrix, array[i], w, h + (9 * i), -1);
							}
						}
						matrix.popPose();
					}
				}
				matrix.popPose();
			}
		}
	}
	
	@SubscribeEvent
	public void handleBoatImmunity(RenderBlockOverlayEvent event){
		Player entity = event.getPlayer();
		if(event.getOverlayType() == OverlayType.FIRE && entity.isOnFire() && entity.getVehicle() instanceof MotorboatEntity){
			MotorboatEntity boat = (MotorboatEntity) entity.getVehicle();
			if(boat.isFireproof){
				event.setCanceled(true);
			}
		}
	}
	
	@SubscribeEvent
	public void handleFireRender(RenderPlayerEvent.Pre event){
		Player entity = event.getPlayer();
		if(entity.isOnFire() && entity.getVehicle() instanceof MotorboatEntity){
			MotorboatEntity boat = (MotorboatEntity) entity.getVehicle();
			if(boat.isFireproof){
				entity.clearFire();
			}
		}
	}
	
	@SubscribeEvent
	public void handleLubricatingMachinesClient(ClientTickEvent event){
		if(event.phase == Phase.END && MCUtil.getLevel() != null){
			CommonEventHandler.handleLubricatingMachines(MCUtil.getLevel());
		}
	}
	
}
