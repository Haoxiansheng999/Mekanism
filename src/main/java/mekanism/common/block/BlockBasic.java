package mekanism.common.block;

import java.util.List;
import java.util.Random;

import mekanism.api.energy.IEnergizedItem;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.common.Mekanism;
import mekanism.common.Tier.BaseTier;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IBoundingBlock;
import mekanism.common.block.states.BlockStateBasic;
import mekanism.common.block.states.BlockStateBasic.BasicBlock;
import mekanism.common.block.states.BlockStateBasic.BasicBlockType;
import mekanism.common.content.tank.TankUpdateProtocol;
import mekanism.common.inventory.InventoryBin;
import mekanism.common.item.ItemBlockBasic;
import mekanism.common.multiblock.IMultiblock;
import mekanism.common.multiblock.IStructuralMultiblock;
import mekanism.common.tile.TileEntityBasicBlock;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.TileEntityBoilerCasing;
import mekanism.common.tile.TileEntityBoilerValve;
import mekanism.common.tile.TileEntityDynamicTank;
import mekanism.common.tile.TileEntityInductionCasing;
import mekanism.common.tile.TileEntityInductionCell;
import mekanism.common.tile.TileEntityInductionProvider;
import mekanism.common.tile.TileEntityPressureDisperser;
import mekanism.common.tile.TileEntityThermalEvaporationController;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import buildcraft.api.tools.IToolWrench;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Block class for handling multiple metal block IDs.
 * 0:0: Osmium Block
 * 0:1: Bronze Block
 * 0:2: Refined Obsidian
 * 0:3: Charcoal Block
 * 0:4: Refined Glowstone
 * 0:5: Steel Block
 * 0:6: Bin
 * 0:7: Teleporter Frame
 * 0:8: Steel Casing
 * 0:9: Dynamic Tank
 * 0:10: Structural Glass
 * 0:11: Dynamic Valve
 * 0:12: Copper Block
 * 0:13: Tin Block
 * 0:14: Thermal Evaporation Controller
 * 0:15: Thermal Evaporation Valve
 * 1:0: Thermal Evaporation Block
 * 1:1: Induction Casing
 * 1:2: Induction Port
 * 1:3: Induction Cell
 * 1:4: Induction Provider
 * 1:5: Superheating Element
 * 1:6: Pressure Disperser
 * 1:7: Boiler Casing
 * 1:8: Boiler Valve
 * @author AidanBrady
 *
 */
public abstract class BlockBasic extends Block//TODO? implements IBlockCTM, ICustomBlockIcon
{
//	public CTMData[][] ctms = new CTMData[16][4];
	
//	public static String ICON_BASE = "mekanism:SteelCasing";

	public BlockBasic()
	{
		super(Material.iron);
		setHardness(5F);
		setResistance(10F);
		setCreativeTab(Mekanism.tabMekanism);
	}

	public static BlockBasic getBlockBasic(BasicBlock block)
	{
		return new BlockBasic() {
			@Override
			public BasicBlock getBasicBlock()
			{
				return block;
			}
		};
	}

	public abstract BasicBlock getBasicBlock();

	public BlockState createBlockState()
	{
		return new BlockStateBasic(this, getProperty());
	}

	public PropertyEnum<BlockStateBasic.BasicBlockType> getProperty()
	{
		return getBasicBlock().getProperty();
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		BlockStateBasic.BasicBlockType type = BlockStateBasic.BasicBlockType.get(getBasicBlock(), meta&0xF);

		return this.getDefaultState().withProperty(getProperty(), type);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		BlockStateBasic.BasicBlockType type = state.getValue(getProperty());
		return type.meta;
	}
/*
	@Override
	public IIcon getIcon(ItemStack stack, int side)
	{
		if(getBlockFromItem(stack.getItem()) == MekanismBlocks.BasicBlock2 && stack.getItemDamage() == 3)
		{
			return icons[3][((ItemBlockBasic)stack.getItem()).getTier(stack).ordinal()];
		}
		else if(getBlockFromItem(stack.getItem()) == MekanismBlocks.BasicBlock2 && stack.getItemDamage() == 4)
		{
			return icons[4][((ItemBlockBasic)stack.getItem()).getTier(stack).ordinal()];
		}
		
		return getIcon(side, stack.getItemDamage());
	}
*/

	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock)
	{
		if(!world.isRemote)
		{
			TileEntity tileEntity = world.getTileEntity(pos);

			if(neighborBlock == this && tileEntity instanceof IMultiblock)
			{
				((IMultiblock)tileEntity).update();
			}

			if(tileEntity instanceof TileEntityBasicBlock)
			{
				((TileEntityBasicBlock)tileEntity).onNeighborChange(neighborBlock);
			}
			
			if(tileEntity instanceof IStructuralMultiblock)
			{
				((IStructuralMultiblock)tileEntity).update();
			}
		}
	}

/*
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register)
	{
		switch(blockType)
		{
			case BASIC_BLOCK_1:
				ctms[7][0] = new CTMData("ctm/TeleporterFrame", this, Arrays.asList(7)).addOtherBlockConnectivities(MekanismBlocks.MachineBlock, Arrays.asList(11)).registerIcons(register);
				ctms[9][0] = new CTMData("ctm/DynamicTank", this, Arrays.asList(9, 11)).registerIcons(register);
				ctms[10][0] = new CTMData("ctm/StructuralGlass", this, Arrays.asList(10)).registerIcons(register);
				ctms[11][0] = new CTMData("ctm/DynamicValve", this, Arrays.asList(11, 9)).registerIcons(register);

				ctms[14][0] = new CTMData("ctm/ThermalEvaporationBlock", this, Arrays.asList(14, 15)).addOtherBlockConnectivities(MekanismBlocks.BasicBlock2, Arrays.asList(0)).addFacingOverride("ctm/ThermalEvaporationController").registerIcons(register);
				ctms[14][1] = new CTMData("ctm/ThermalEvaporationBlock", this, Arrays.asList(14, 15)).addOtherBlockConnectivities(MekanismBlocks.BasicBlock2, Arrays.asList(0)).addFacingOverride("ctm/ThermalEvaporationControllerOn").registerIcons(register);
				ctms[15][0] = new CTMData("ctm/ThermalEvaporationValve", this, Arrays.asList(15, 14)).addOtherBlockConnectivities(MekanismBlocks.BasicBlock2, Arrays.asList(0)).registerIcons(register);

				icons[0][0] = register.registerIcon("mekanism:OsmiumBlock");
				icons[1][0] = register.registerIcon("mekanism:BronzeBlock");
				icons[2][0] = register.registerIcon("mekanism:RefinedObsidian");
				icons[3][0] = register.registerIcon("mekanism:CoalBlock");
				icons[4][0] = register.registerIcon("mekanism:RefinedGlowstone");
				icons[5][0] = register.registerIcon("mekanism:SteelBlock");
				
				MekanismRenderer.loadDynamicTextures(register, "Bin", icons[6], DefIcon.getActivePair(register.registerIcon("mekanism:BinSide"), 3, 4, 5),
						new DefIcon(register.registerIcon("mekanism:BinTop"), 0), new DefIcon(register.registerIcon("mekanism:BinTopOn"), 6));
				
				icons[7][0] = ctms[7][0].mainTextureData.icon;
				icons[8][0] = register.registerIcon("mekanism:SteelCasing");
				icons[9][0] = ctms[9][0].mainTextureData.icon;
				icons[10][0] = ctms[10][0].mainTextureData.icon;
				icons[11][0] = ctms[11][0].mainTextureData.icon;
				icons[12][0] = register.registerIcon("mekanism:CopperBlock");
				icons[13][0] = register.registerIcon("mekanism:TinBlock");
				icons[14][0] = ctms[14][0].facingOverride.icon;
				icons[14][1] = ctms[14][1].facingOverride.icon;
				icons[14][2] = ctms[14][0].mainTextureData.icon;
				icons[15][0] = ctms[15][0].mainTextureData.icon;
				break;
			case BASIC_BLOCK_2:
				ctms[0][0] = new CTMData("ctm/ThermalEvaporationBlock", this, Arrays.asList(0)).addOtherBlockConnectivities(MekanismBlocks.BasicBlock, Arrays.asList(14, 15)).registerIcons(register);
				ctms[1][0] = new CTMData("ctm/InductionCasing", this, Arrays.asList(1, 2)).registerIcons(register);
				ctms[2][0] = new CTMData("ctm/InductionPortInput", this, Arrays.asList(1, 2)).registerIcons(register);
				ctms[2][1] = new CTMData("ctm/InductionPortOutput", this, Arrays.asList(1, 2)).registerIcons(register);
				ctms[3][0] = new CTMData("ctm/InductionCellBasic", this, Arrays.asList(3, 4)).registerIcons(register).setRenderConvexConnections();
				ctms[3][1] = new CTMData("ctm/InductionCellAdvanced", this, Arrays.asList(3, 4)).registerIcons(register).setRenderConvexConnections();
				ctms[3][2] = new CTMData("ctm/InductionCellElite", this, Arrays.asList(3, 4)).registerIcons(register).setRenderConvexConnections();
				ctms[3][3] = new CTMData("ctm/InductionCellUltimate", this, Arrays.asList(3, 4)).registerIcons(register).setRenderConvexConnections();
				ctms[4][0] = new CTMData("ctm/InductionProviderBasic", this, Arrays.asList(3, 4)).registerIcons(register).setRenderConvexConnections();
				ctms[4][1] = new CTMData("ctm/InductionProviderAdvanced", this, Arrays.asList(3, 4)).registerIcons(register).setRenderConvexConnections();
				ctms[4][2] = new CTMData("ctm/InductionProviderElite", this, Arrays.asList(3, 4)).registerIcons(register).setRenderConvexConnections();
				ctms[4][3] = new CTMData("ctm/InductionProviderUltimate", this, Arrays.asList(3, 4)).registerIcons(register).setRenderConvexConnections();
				ctms[5][0] = new CTMData("ctm/SuperheatingElement", this, Arrays.asList(5)).registerIcons(register).setRenderConvexConnections();
				ctms[7][0] = new CTMData("ctm/BoilerCasing", this, Arrays.asList(7, 8)).registerIcons(register);
				ctms[8][0] = new CTMData("ctm/BoilerValve", this, Arrays.asList(7, 8)).registerIcons(register);
				
				icons[6][0] = register.registerIcon("mekanism:PressureDisperser");
				
				icons[0][0] = ctms[0][0].mainTextureData.icon;
				icons[1][0] = ctms[1][0].mainTextureData.icon;
				icons[2][0] = ctms[2][0].mainTextureData.icon;
				icons[2][1] = ctms[2][1].mainTextureData.icon;
				icons[3][0] = ctms[3][0].mainTextureData.icon;
				icons[3][1] = ctms[3][1].mainTextureData.icon;
				icons[3][2] = ctms[3][2].mainTextureData.icon;
				icons[3][3] = ctms[3][3].mainTextureData.icon;
				icons[4][0] = ctms[4][0].mainTextureData.icon;
				icons[4][1] = ctms[4][1].mainTextureData.icon;
				icons[4][2] = ctms[4][2].mainTextureData.icon;
				icons[4][3] = ctms[4][3].mainTextureData.icon;
				icons[5][0] = ctms[5][0].mainTextureData.icon;
				icons[7][0] = ctms[7][0].mainTextureData.icon;
				icons[8][0] = ctms[8][0].mainTextureData.icon;
				
				break;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess world, BlockPos pos, int side)
	{
		int meta = world.getBlockMetadata(pos);

		switch(blockType)
		{
			case BASIC_BLOCK_1:
				switch(meta)
				{
					case 6:
						TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(pos);

						boolean active = MekanismUtils.isActive(world, pos);
						return icons[meta][MekanismUtils.getBaseOrientation(side, tileEntity.facing)+(active ? 6 : 0)];
					case 14:
						TileEntityThermalEvaporationController tileEntity1 = (TileEntityThermalEvaporationController)world.getTileEntity(pos);

						if(side == tileEntity1.facing)
						{
							return MekanismUtils.isActive(world, pos) ? icons[meta][1] : icons[meta][0];
						} 
						else {
							return icons[meta][2];
						}
					default:
						return getIcon(side, meta);
				}
			case BASIC_BLOCK_2:
				switch(meta)
				{
					case 2:
						TileEntityInductionPort tileEntity = (TileEntityInductionPort)world.getTileEntity(pos);
						return icons[meta][tileEntity.mode ? 1 : 0];
					case 3:
						TileEntityInductionCell tileEntity1 = (TileEntityInductionCell)world.getTileEntity(pos);
						return icons[meta][tileEntity1.tier.ordinal()];
					case 4:
						TileEntityInductionProvider tileEntity2 = (TileEntityInductionProvider)world.getTileEntity(pos);
						return icons[meta][tileEntity2.tier.ordinal()];
					default:
						return getIcon(side, meta);
				}
		}

		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta)
	{
		switch(blockType)
		{
			case BASIC_BLOCK_1:
				switch(meta)
				{
					case 6:
						return icons[meta][side];
					case 14:
						if(side == 2)
						{
							return icons[meta][0];
						} 
						else {
							return icons[meta][2];
						}
					default:
						return icons[meta][0];
				}
			case BASIC_BLOCK_2:
				return icons[meta][0];
			default:
				return icons[meta][0];
		}
	}
*/

	@Override
	public int damageDropped(IBlockState state)
	{
		return getMetaFromState(state);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs creativetabs, List<ItemStack> list)
	{
		for(BasicBlockType type : BasicBlockType.values())
		{
			if(type.blockType == getBasicBlock())
			{
				switch(type)
				{
					case INDUCTION_CELL:
					case INDUCTION_PROVIDER:
						for(BaseTier tier : BaseTier.values())
						{
							if(tier.isObtainable())
							{
								ItemStack stack = new ItemStack(item, 1, type.meta);
								((ItemBlockBasic)stack.getItem()).setTier(stack, tier);
								list.add(stack);
							}
						}
						
						break;
					default:
						list.add(new ItemStack(item, 1, type.meta));
				}
			}
		}
	}

	@Override
	public boolean canCreatureSpawn(IBlockAccess world, BlockPos pos, SpawnPlacementType type)
	{
		IBlockState state = world.getBlockState(pos);
		int meta = state.getBlock().getMetaFromState(state);

		switch(getBasicBlock())
		{
			case BASIC_BLOCK_1:
				switch(meta)
				{
					case 9:
					case 11:
						TileEntityDynamicTank tileEntity = (TileEntityDynamicTank)world.getTileEntity(pos);

						if(tileEntity != null)
						{
							if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
							{
								if(tileEntity.structure != null)
								{
									return false;
								}
							} 
							else {
								if(tileEntity.clientHasStructure)
								{
									return false;
								}
							}
						}
					default:
						return super.canCreatureSpawn(world, pos, type);
				}
			case BASIC_BLOCK_2:
				switch(meta)
				{
					case 1:
					case 2:
						TileEntityInductionCasing tileEntity = (TileEntityInductionCasing)world.getTileEntity(pos);

						if(tileEntity != null)
						{
							if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
							{
								if(tileEntity.structure != null)
								{
									return false;
								}
							} 
							else {
								if(tileEntity.clientHasStructure)
								{
									return false;
								}
							}
						}
					default:
						return super.canCreatureSpawn(world, pos, type);
				}
			default:
				return super.canCreatureSpawn(world, pos, type);
		}
	}

	@Override
	public void onBlockClicked(World world, BlockPos pos, EntityPlayer player)
	{
		BasicBlockType type = BasicBlockType.get(world.getBlockState(pos));

		if(!world.isRemote && type == BasicBlockType.BIN)
		{
			TileEntityBin bin = (TileEntityBin)world.getTileEntity(pos);
			MovingObjectPosition mop = MekanismUtils.rayTrace(world, player);

			if(mop != null && mop.sideHit == bin.facing)
			{
				if(bin.bottomStack != null)
				{
					if(!player.isSneaking())
					{
						world.spawnEntityInWorld(new EntityItem(world, player.posX, player.posY, player.posZ, bin.removeStack().copy()));
					}
					else {
						world.spawnEntityInWorld(new EntityItem(world, player.posX, player.posY, player.posZ, bin.remove(1).copy()));
					}
				}
			}
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityplayer, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		BasicBlockType type = BasicBlockType.get(state);
		TileEntity tile = world.getTileEntity(pos);

		if(type == BasicBlockType.REFINED_OBSIDIAN)
		{
			if(entityplayer.isSneaking())
			{
				entityplayer.openGui(Mekanism.instance, 1, world, pos.getX(), pos.getY(), pos.getZ());
				return true;
			}
		}
		
		if(world.isRemote)
		{
			return true;
		}

		if(tile instanceof TileEntityThermalEvaporationController)
		{
			if(!entityplayer.isSneaking())
			{
				entityplayer.openGui(Mekanism.instance, 33, world, pos.getX(), pos.getY(), pos.getZ());
				return true;
			}
		}
		else if(tile instanceof TileEntityBin)
		{
			TileEntityBin bin = (TileEntityBin)tile;

			if(entityplayer.getCurrentEquippedItem() != null && MekanismUtils.hasUsableWrench(entityplayer, pos))
			{
				Item tool = entityplayer.getCurrentEquippedItem().getItem();
				
				if(entityplayer.isSneaking())
				{
					dismantleBlock(world, pos, false);
					return true;
				}

				if(MekanismUtils.isBCWrench(tool))
				{
					((IToolWrench)tool).wrenchUsed(entityplayer, pos);
				}

				int change = bin.facing.rotateY().ordinal();

				bin.setFacing((short)change);
				world.notifyNeighborsOfStateChange(pos, this);
				
				return true;
			}

			if(bin.getItemCount() < bin.MAX_STORAGE)
			{
				if(bin.addTicks == 0 && entityplayer.getCurrentEquippedItem() != null)
				{
					if(entityplayer.getCurrentEquippedItem() != null)
					{
						ItemStack remain = bin.add(entityplayer.getCurrentEquippedItem());
						entityplayer.setCurrentItemOrArmor(0, remain);
						bin.addTicks = 5;
					}
				}
				else if(bin.addTicks > 0 && bin.getItemCount() > 0)
				{
					ItemStack[] inv = entityplayer.inventory.mainInventory;

					for(int i = 0; i < inv.length; i++)
					{
						if(bin.getItemCount() == bin.MAX_STORAGE)
						{
							break;
						}

						if(inv[i] != null)
						{
							ItemStack remain = bin.add(inv[i]);
							inv[i] = remain;
							bin.addTicks = 5;
						}

						((EntityPlayerMP)entityplayer).sendContainerToPlayer(entityplayer.openContainer);
					}
				}
			}

			return true;
		}
		else if(tile instanceof IMultiblock)
		{
			return ((IMultiblock)world.getTileEntity(pos)).onActivate(entityplayer);
		}
		else if(tile instanceof IStructuralMultiblock)
		{
			return ((IStructuralMultiblock)world.getTileEntity(pos)).onActivate(entityplayer);
		}

		return false;
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		return BasicBlockType.get(world.getBlockState(pos)) != BasicBlockType.STRUCTURAL_GLASS;
	}

	public static boolean manageInventory(EntityPlayer player, TileEntityDynamicTank tileEntity)
	{
		ItemStack itemStack = player.getCurrentEquippedItem();

		if(itemStack != null && tileEntity.structure != null)
		{
			if(FluidContainerRegistry.isEmptyContainer(itemStack))
			{
				if(tileEntity.structure.fluidStored != null && tileEntity.structure.fluidStored.amount >= FluidContainerRegistry.BUCKET_VOLUME)
				{
					ItemStack filled = FluidContainerRegistry.fillFluidContainer(tileEntity.structure.fluidStored, itemStack);

					if(filled != null)
					{
						if(player.capabilities.isCreativeMode)
						{
							tileEntity.structure.fluidStored.amount -= FluidContainerRegistry.getFluidForFilledItem(filled).amount;

							if(tileEntity.structure.fluidStored.amount == 0)
							{
								tileEntity.structure.fluidStored = null;
							}

							return true;
						}

						if(itemStack.stackSize > 1)
						{
							if(player.inventory.addItemStackToInventory(filled))
							{
								itemStack.stackSize--;

								tileEntity.structure.fluidStored.amount -= FluidContainerRegistry.getFluidForFilledItem(filled).amount;

								if(tileEntity.structure.fluidStored.amount == 0)
								{
									tileEntity.structure.fluidStored = null;
								}

								return true;
							}
						}
						else if(itemStack.stackSize == 1)
						{
							player.setCurrentItemOrArmor(0, filled);

							tileEntity.structure.fluidStored.amount -= FluidContainerRegistry.getFluidForFilledItem(filled).amount;

							if(tileEntity.structure.fluidStored.amount == 0)
							{
								tileEntity.structure.fluidStored = null;
							}

							return true;
						}
					}
				}
			}
			else if(FluidContainerRegistry.isFilledContainer(itemStack))
			{
				FluidStack itemFluid = FluidContainerRegistry.getFluidForFilledItem(itemStack);
				int max = tileEntity.structure.volume*TankUpdateProtocol.FLUID_PER_TANK;

				if(tileEntity.structure.fluidStored == null || (tileEntity.structure.fluidStored.isFluidEqual(itemFluid) && (tileEntity.structure.fluidStored.amount+itemFluid.amount <= max)))
				{
					boolean filled = false;
					
					if(player.capabilities.isCreativeMode)
					{
						filled = true;
					}
					else {
						ItemStack containerItem = itemStack.getItem().getContainerItem(itemStack);
	
						if(containerItem != null)
						{
							if(itemStack.stackSize == 1)
							{
								player.setCurrentItemOrArmor(0, containerItem);
								filled = true;
							}
							else {
								if(player.inventory.addItemStackToInventory(containerItem))
								{
									itemStack.stackSize--;
	
									filled = true;
								}
							}
						}
						else {
							itemStack.stackSize--;
	
							if(itemStack.stackSize == 0)
							{
								player.setCurrentItemOrArmor(0, null);
							}
	
							filled = true;
						}
					}

					if(filled)
					{
						if(tileEntity.structure.fluidStored == null)
						{
							tileEntity.structure.fluidStored = itemFluid;
						}
						else {
							tileEntity.structure.fluidStored.amount += itemFluid.amount;
						}
						
						return true;
					}
				}
			}
		}

		return false;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public int getRenderType()
	{
		return 3;
	}

	@Override
	public int getLightValue(IBlockAccess world, BlockPos pos)
	{
		TileEntity tileEntity = world.getTileEntity(pos);
		IBlockState state = world.getBlockState(pos);
		int metadata = state.getBlock().getMetaFromState(state);

		if(tileEntity instanceof IActiveState)
		{
			if(((IActiveState)tileEntity).getActive() && ((IActiveState)tileEntity).lightUpdate())
			{
				return 15;
			}
		}

		if(getBasicBlock() == BasicBlock.BASIC_BLOCK_1)
		{
			switch(metadata)
			{
				case 2:
					return 8;
				case 4:
					return 15;
				case 7:
					return 12;
			}
		}

		return 0;
	}

	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		BasicBlockType type = BasicBlockType.get(state);
		
		return type != null && type.tileEntityClass != null;
	}
	
	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state)
	{
		TileEntity tileEntity = world.getTileEntity(pos);

		if(!world.isRemote)
		{
			if(tileEntity instanceof TileEntityBasicBlock)
			{
				((TileEntityBasicBlock)tileEntity).onAdded();
			}
		}
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state)
	{
		if(BasicBlockType.get(state) == null)
		{
			return null;
		}

		return BasicBlockType.get(state).create();
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		if(world.getTileEntity(pos) instanceof TileEntityBasicBlock)
		{
			TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(pos);
			int side = MathHelper.floor_double((placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
			int height = Math.round(placer.rotationPitch);
			int change = 3;

			if(tileEntity.canSetFacing(0) && tileEntity.canSetFacing(1))
			{
				if(height >= 65)
				{
					change = 1;
				}
				else if(height <= -65)
				{
					change = 0;
				}
			}

			if(change != 0 && change != 1)
			{
				switch(side)
				{
					case 0: change = 2; break;
					case 1: change = 5; break;
					case 2: change = 3; break;
					case 3: change = 4; break;
				}
			}

			tileEntity.setFacing((short)change);
			tileEntity.redstone = world.isBlockIndirectlyGettingPowered(pos) > 0;

			if(tileEntity instanceof IBoundingBlock)
			{
				((IBoundingBlock)tileEntity).onPlace();
			}
		}

		world.markBlockRangeForRenderUpdate(pos, pos.add(1,1,1));
		world.checkLightFor(EnumSkyBlock.BLOCK, pos);
	    world.checkLightFor(EnumSkyBlock.SKY, pos);

		if(!world.isRemote && world.getTileEntity(pos) != null)
		{
			TileEntity tileEntity = world.getTileEntity(pos);

			if(tileEntity instanceof IMultiblock)
			{
				((IMultiblock)tileEntity).update();
			}
			
			if(tileEntity instanceof IStructuralMultiblock)
			{
				((IStructuralMultiblock)tileEntity).update();
			}
		}
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos, EntityPlayer player)
	{
		IBlockState state = world.getBlockState(pos);
		BasicBlockType type = BasicBlockType.get(state);
		ItemStack ret = new ItemStack(this, 1, state.getBlock().getMetaFromState(state));

		if(type == BasicBlockType.BIN)
		{
			TileEntityBin tileEntity = (TileEntityBin)world.getTileEntity(pos);
			InventoryBin inv = new InventoryBin(ret);

			inv.setItemCount(tileEntity.getItemCount());

			if(tileEntity.getItemCount() > 0)
			{
				inv.setItemType(tileEntity.itemType);
			}
		}
		else if(type == BasicBlockType.INDUCTION_CELL)
		{
			TileEntityInductionCell tileEntity = (TileEntityInductionCell)world.getTileEntity(pos);
			((ItemBlockBasic)ret.getItem()).setTier(ret, tileEntity.tier.getBaseTier());
		}
		else if(type == BasicBlockType.INDUCTION_PROVIDER)
		{
			TileEntityInductionProvider tileEntity = (TileEntityInductionProvider)world.getTileEntity(pos);
			((ItemBlockBasic)ret.getItem()).setTier(ret, tileEntity.tier.getBaseTier());
		}
		
		TileEntity tileEntity = world.getTileEntity(pos);
		
		if(tileEntity instanceof IStrictEnergyStorage)
		{
			IEnergizedItem energizedItem = (IEnergizedItem)ret.getItem();
			energizedItem.setEnergy(ret, ((IStrictEnergyStorage)tileEntity).getEnergy());
		}

		return ret;
	}

	@Override
	public Item getItemDropped(IBlockState state, Random random, int fortune)
	{
		return null;
	}

	@Override
	public boolean removedByPlayer(World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
	{
		if(!player.capabilities.isCreativeMode && !world.isRemote && willHarvest)
		{

			float motion = 0.7F;
			double motionX = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionY = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionZ = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;

			EntityItem entityItem = new EntityItem(world, pos.getX() + motionX, pos.getY() + motionY, pos.getZ() + motionZ, getPickBlock(null, world, pos, player));

			world.spawnEntityInWorld(entityItem);
		}

		return world.setBlockToAir(pos);
	}

	public ItemStack dismantleBlock(World world, BlockPos pos, boolean returnBlock)
	{
		ItemStack itemStack = getPickBlock(null, world, pos, null);

		world.setBlockToAir(pos);

		if(!returnBlock)
		{
			float motion = 0.7F;
			double motionX = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionY = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionZ = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;

			EntityItem entityItem = new EntityItem(world, pos.getX() + motionX, pos.getY() + motionY, pos.getZ() + motionZ, itemStack);

			world.spawnEntityInWorld(entityItem);
		}

		return itemStack;
	}

/*
	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess world, BlockPos pos, int side)
	{
		Coord4D obj = new Coord4D(pos).offset(EnumFacing.getFront(side).getOpposite());
		
		if(BasicBlockType.get(this, world.getBlockMetadata(x, y, z)) == BasicBlockType.STRUCTURAL_GLASS)
		{
			return ctms[10][0].shouldRenderSide(world, pos, side);
		}
		else {
			return super.shouldSideBeRendered(world, pos, side);
		}
	}
*/

	@Override
	public EnumFacing[] getValidRotations(World world, BlockPos pos)
	{
		TileEntity tile = world.getTileEntity(pos);
		EnumFacing[] valid = new EnumFacing[6];
		
		if(tile instanceof TileEntityBasicBlock)
		{
			TileEntityBasicBlock basicTile = (TileEntityBasicBlock)tile;
			
			for(EnumFacing dir : EnumFacing.VALUES)
			{
				if(basicTile.canSetFacing(dir.ordinal()))
				{
					valid[dir.ordinal()] = dir;
				}
			}
		}
		
		return valid;
	}

	@Override
	public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis)
	{
		TileEntity tile = world.getTileEntity(pos);
		
		if(tile instanceof TileEntityBasicBlock)
		{
			TileEntityBasicBlock basicTile = (TileEntityBasicBlock)tile;
			
			if(basicTile.canSetFacing(axis.ordinal()))
			{
				basicTile.setFacing((short)axis.ordinal());
				return true;
			}
		}
		
		return false;
	}

/*
	@Override
	public CTMData getCTMData(IBlockAccess world, BlockPos pos, int meta)
	{
		if(ctms[meta][1] != null && MekanismUtils.isActive(world, pos))
		{
			return ctms[meta][1];
		}
		
		BasicBlockType type = BasicBlockType.get(this, world.getBlockMetadata(x, y, z));

		if(type == BasicBlockType.INDUCTION_CELL)
		{
			TileEntityInductionCell tileEntity = (TileEntityInductionCell)world.getTileEntity(pos);
			return ctms[meta][tileEntity.tier.ordinal()];
		}
		else if(type == BasicBlockType.INDUCTION_PROVIDER)
		{
			TileEntityInductionProvider tileEntity = (TileEntityInductionProvider)world.getTileEntity(pos);
			return ctms[meta][tileEntity.tier.ordinal()];
		}

		return ctms[meta][0];
	}
*/
}