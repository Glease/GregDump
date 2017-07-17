import gregtech.api.interfaces.internal.IGT_RecipeAdder
import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.FluidStack
import java.util.*
import kotlin.collections.ArrayList

typealias ColumnBuffer = MutableList<String>

/**
 * @author glease
 * @since 1.0
 */
class DelegatingGT_RecipeAdder(val backing: IGT_RecipeAdder) : IGT_RecipeAdder by backing {
	val cache = HashMap<Int, ColumnBuffer>()
	fun getFromCache(size: Int): ColumnBuffer {
		return cache.computeIfAbsent(size) { (1..it).mapTo(ArrayList()) { "" } }
	}

	override fun addDistillationTowerRecipe(aInput: FluidStack?, aOutputs: Array<out FluidStack>, aOutput2: ItemStack?, aDuration: Int, aEUt: Int): Boolean {
		val buf = getFromCache(13 * 2)
		buf.writeSlot(aInput, 0)
		buf.writeSlot(aOutput2, 1)
		buf.writeSlots(aOutputs, 2, 11)
		dedicatedDistillation.writeColumn(buf)
		return backing.addDistillationTowerRecipe(aInput, aOutputs, aOutput2, aDuration, aEUt)
	}

	override fun addDistilleryRecipe(aCircuit: ItemStack?, aInput: FluidStack?, aOutput: FluidStack?, aSolidOutput: ItemStack?, aDuration: Int, aEUt: Int, aHidden: Boolean): Boolean {
		val buf = getFromCache(7)
		buf.writeSlot(aInput, 0)
		buf.writeSlot(aSolidOutput, 1)
		buf.writeSlot(aOutput, 2)
		buf[6] = aHidden.toString()
		dedicatedDistillery.writeColumn(buf)
		return backing.addDistilleryRecipe(aCircuit, aInput, aOutput, aSolidOutput, aDuration, aEUt, aHidden)
	}

	override fun addDistilleryRecipe(aCircuit: ItemStack?, aInput: FluidStack?, aOutput: FluidStack?, aDuration: Int, aEUt: Int, aHidden: Boolean): Boolean {
		val buf = getFromCache(7)
		buf.writeSlot(aInput, 0)
		buf.clearSlot(1)
		buf.writeSlot(aOutput, 2)
		buf[6] = aHidden.toString()
		dedicatedDistillery.writeColumn(buf)
		return backing.addDistilleryRecipe(aCircuit, aInput, aOutput, aDuration, aEUt, aHidden)
	}

	override fun addDistilleryRecipe(circuitConfig: Int, aInput: FluidStack?, aOutput: FluidStack?, aSolidOutput: ItemStack?, aDuration: Int, aEUt: Int, aHidden: Boolean): Boolean {
		val buf = getFromCache(7)
		buf.writeSlot(aInput, 0)
		buf.writeSlot(aSolidOutput, 1)
		buf.writeSlot(aOutput, 2)
		buf[6] = aHidden.toString()
		dedicatedDistillery.writeColumn(buf)
		return backing.addDistilleryRecipe(circuitConfig, aInput, aOutput, aSolidOutput, aDuration, aEUt, aHidden)
	}

	override fun addDistilleryRecipe(aCircuit: Int, aInput: FluidStack?, aOutput: FluidStack?, aDuration: Int, aEUt: Int, aHidden: Boolean): Boolean {
		val buf = getFromCache(7)
		buf.writeSlot(aInput, 0)
		buf.clearSlot(1)
		buf.writeSlot(aOutput, 2)
		buf[6] = aHidden.toString()
		dedicatedDistillery.writeColumn(buf)
		return backing.addDistilleryRecipe(aCircuit, aInput, aOutput, aDuration, aEUt, aHidden)
	}

	override fun addUniversalDistillationRecipe(aInput: FluidStack?, aOutputs: Array<out FluidStack>, aOutput2: ItemStack?, aDuration: Int, aEUt: Int): Boolean {
		val buf = getFromCache(13 * 2)
		buf.writeSlot(aInput, 0)
		buf.writeSlot(aOutput2, 1)
		buf.writeSlots(aOutputs, 2, 11)
		universalDistillation.writeColumn(buf)
		for (i in 0..Math.min(aOutputs.size, 11) - 1) {
			backing.addDistilleryRecipe(i + 1, aInput, aOutputs[i], aOutput2, aDuration * 2, aEUt / 4, false)
		}
		return backing.addDistillationTowerRecipe(aInput, aOutputs, aOutput2, aDuration, aEUt)
	}

	fun ColumnBuffer.writeSlot(first: String, last: String, offset: Int) {
		this[offset * 2] = first
		this[offset * 2 + 1] = last
	}

	fun ColumnBuffer.writeSlot(stack: FluidStack?, offset: Int) {
		if (stack == null)
			clearSlot(offset)
		else
			writeSlot(stack.unlocalizedName, stack.amount.toString(), offset)
	}

	fun ColumnBuffer.writeSlot(stack: ItemStack?, offset: Int) {
		if (stack == null)
			clearSlot(offset)
		else
			writeSlot(stack.unlocalizedName, stack.stackSize.toString(), offset)
	}

	fun ColumnBuffer.clearSlot(offset: Int) {
		writeSlot("", "", offset)
	}

	/**
	 * @param offset the offset of SLOT, not COLUMN! Each stack takes one SLOT, or two COLUMN! Offset 0 means fill
	 * from COLUMN 0 to 2n! Offset 1 means fill from 2 to 2n+2.
	 * @param expected the expected length of this array. nonexistence one will be filled with empty string
	 */
	fun ColumnBuffer.writeSlots(stacks: Array<out FluidStack>, offset: Int, expected: Int) {
		var i = 0
		while (i < stacks.size) {
			writeSlot(stacks[i], i + offset)
			i++
		}
		while (i < expected) {
			clearSlot(i)
			i++
		}
	}

	val dedicatedDistillery = CSVHelper(System.getProperty("small"),
	                                    "Input", "Input Amount", "Solid", "Solid amount", "Output", "Output Amount",
	                                    "Hidden")
	val dedicatedDistillation = CSVHelper(System.getProperty("tower"), *make())
	val universalDistillation = CSVHelper(System.getProperty("all"), *make())
	private fun make(): Array<String> {
		var i = 0
		val a = Array(26) { "" }
		a[0] = "Input"
		a[1] = "Input Amount"
		a[2] = "Solid Output"
		a[3] = "Solid Output Amount"
		while (i < 11) {
			a[i * 2 + 4] = "Output ${i + 1}"
			a[i * 2 + 5] = "Output ${i + 1} Amount"
			i++
		}
		return a
	}
}