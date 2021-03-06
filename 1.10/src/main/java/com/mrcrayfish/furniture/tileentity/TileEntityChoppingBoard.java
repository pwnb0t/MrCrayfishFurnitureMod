/**
 * MrCrayfish's Furniture Mod
 * Copyright (C) 2016  MrCrayfish (http://www.mrcrayfish.com/)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mrcrayfish.furniture.tileentity;

import com.mrcrayfish.furniture.api.RecipeAPI;
import com.mrcrayfish.furniture.api.RecipeData;
import com.mrcrayfish.furniture.gui.inventory.ISimpleInventory;
import com.mrcrayfish.furniture.init.FurnitureSounds;
import com.mrcrayfish.furniture.util.TileEntityUtil;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;

public class TileEntityChoppingBoard extends TileEntity implements ISimpleInventory
{
	private ItemStack food = null;

	public void setFood(ItemStack food)
	{
		this.food = food;
	}

	public ItemStack getFood()
	{
		return food;
	}

	public boolean chopFood()
	{
		if (food != null)
		{
			RecipeData data = RecipeAPI.getChoppingBoardRecipeFromInput(food);
			if (data != null)
			{
				if (!worldObj.isRemote)
				{
					EntityItem entityItem = new EntityItem(worldObj, pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5, data.getOutput().copy());
					worldObj.spawnEntityInWorld(entityItem);
					worldObj.playSound(pos.getX(), pos.getY(), pos.getZ(), FurnitureSounds.knife_chop, SoundCategory.BLOCKS, 0.75F, 1.0F, true);
				}
				setFood(null);
				TileEntityUtil.markBlockForUpdate(worldObj, pos);
				return true;
			}
		}
		return false;
	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompound)
	{
		super.readFromNBT(tagCompound);
		if(tagCompound.hasKey("Food"))
		{
			NBTTagCompound nbt = tagCompound.getCompoundTag("Food");
			food = ItemStack.loadItemStackFromNBT(nbt);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompound)
	{
		super.writeToNBT(tagCompound);
		NBTTagCompound nbt = new NBTTagCompound();
		if(food != null)
		{
			food.writeToNBT(nbt);
			tagCompound.setTag("Food", nbt);
		}
		return tagCompound;
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
	{
		NBTTagCompound tagCom = pkt.getNbtCompound();
		this.readFromNBT(tagCom);
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket()
	{
		NBTTagCompound tagCom = new NBTTagCompound();
		this.writeToNBT(tagCom);
		return new SPacketUpdateTileEntity(pos, getBlockMetadata(), tagCom);
	}
	
	@Override
	public NBTTagCompound getUpdateTag() 
	{
		return writeToNBT(new NBTTagCompound());
	}

	@Override
	public int getSize()
	{
		return 1;
	}

	@Override
	public ItemStack getItem(int i)
	{
		return food;
	}

	@Override
	public void clear()
	{
		food = null;
	}
}
