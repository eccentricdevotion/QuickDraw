/*
 * Kristian S. Stangeland aadnk
 * Norway
 * kristian@comphenix.net
 * http://www.comphenix.net/
 */
package me.eccentric_nz.plugins.quickdraw;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class QuickdrawInventory {

    public static String toBase64(Inventory inventory) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutput = new DataOutputStream(outputStream);
        net.minecraft.server.v1_5_R1.NBTTagList itemList = new net.minecraft.server.v1_5_R1.NBTTagList();

        // Save every element in the list
        for (int i = 0; i < inventory.getSize(); i++) {
            net.minecraft.server.v1_5_R1.NBTTagCompound outputObject = new net.minecraft.server.v1_5_R1.NBTTagCompound();
            org.bukkit.craftbukkit.v1_5_R1.inventory.CraftItemStack craft = getCraftVersion(inventory.getItem(i));

            // Convert the item stack to a NBT compound
            if (craft != null) {
                org.bukkit.craftbukkit.v1_5_R1.inventory.CraftItemStack.asNMSCopy(craft).save(outputObject);
            }
            itemList.add(outputObject);
        }

        // Now save the list
        net.minecraft.server.v1_5_R1.NBTBase.a(itemList, dataOutput);

        // Serialize that array
        return Base64Coder.encodeLines(outputStream.toByteArray());
    }

    public static Inventory fromBase64(String data) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
        net.minecraft.server.v1_5_R1.NBTTagList itemList = (net.minecraft.server.v1_5_R1.NBTTagList) net.minecraft.server.v1_5_R1.NBTBase.b(new DataInputStream(inputStream));
        Inventory inventory = new org.bukkit.craftbukkit.v1_5_R1.inventory.CraftInventoryCustom(null, itemList.size());

        for (int i = 0; i < itemList.size(); i++) {
            net.minecraft.server.v1_5_R1.NBTTagCompound inputObject = (net.minecraft.server.v1_5_R1.NBTTagCompound) itemList.get(i);

            if (!inputObject.isEmpty()) {
                inventory.setItem(i, org.bukkit.craftbukkit.v1_5_R1.inventory.CraftItemStack.asCraftMirror(
                        net.minecraft.server.v1_5_R1.ItemStack.createStack(inputObject)));
            }
        }

        // Serialize that array
        return inventory;
    }

    private static org.bukkit.craftbukkit.v1_5_R1.inventory.CraftItemStack getCraftVersion(ItemStack stack) {
        if (stack instanceof org.bukkit.craftbukkit.v1_5_R1.inventory.CraftItemStack) {
            return (org.bukkit.craftbukkit.v1_5_R1.inventory.CraftItemStack) stack;
        } else if (stack != null) {
            return org.bukkit.craftbukkit.v1_5_R1.inventory.CraftItemStack.asCraftCopy(stack);
        } else {
            return null;
        }
    }
}
