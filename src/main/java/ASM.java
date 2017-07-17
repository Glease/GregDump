import gregtech.api.interfaces.internal.IGT_RecipeAdder;
import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.ListIterator;
import java.util.Objects;

/**
 * @author glease
 * @since 1.0
 */
public class ASM implements IClassTransformer, Opcodes {
	static Logger log = LogManager.getLogger("Dump ASM");

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if ("gregtech.GT_Mod".equals(name) || "gregtech.GT_Mod".equals(transformedName)) {
			// we are good to start
			log.info("Begin transformation of GT_Mod");
			ClassReader cr = new ClassReader(basicClass);
			ClassNode cn = new ClassNode(ASM5);
			cr.accept(cn, 0);

			MethodNode mn = cn.methods.stream().filter(m -> "<init>".equals(m.name)).findAny().orElseThrow(AssertionError::new);
			log.debug("Constructor found");
			InsnList list = mn.instructions;
			ListIterator<AbstractInsnNode> iter = list.iterator();
			while (iter.hasNext()) {
				AbstractInsnNode next = iter.next();
				if (next instanceof MethodInsnNode) {
					MethodInsnNode insn = (MethodInsnNode) next;
					if ("name".equals(insn.name)) {
						// before texture class force load
						// after recipe adder created
						log.info("Identified location");
						list.insert(insn, new MethodInsnNode(INVOKESTATIC, "ASM", "callback", "()V", false));
						break;
					}
				}
			}
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
			StringWriter sw = new StringWriter();
			log.debug("Writing class bytes");
			cn.accept(cw);
			return cw.toByteArray();
		}
		return basicClass;
	}

	public static void callback() {
		log.trace("Call hook hit.");
		try {
			Class<?> gregapi = Class.forName("gregtech.api.GregTech_API");
			Field field1 = Arrays.stream(gregapi.getFields())
					               .filter(f -> f.getType().getName().contains("IGT_RecipeAdder"))
					               .findAny().orElseThrow(AssertionError::new);
			IGT_RecipeAdder recipeAdder = (IGT_RecipeAdder) field1.get(null);
			DelegatingGT_RecipeAdder d = new DelegatingGT_RecipeAdder(recipeAdder);
			field1.set(null, d);
			Class<?> gtValues = Class.forName("gregtech.api.enums.GT_Values");
			Arrays.stream(gtValues.getFields())
					.filter(f -> Objects.equals(f.getName(), "RA"))
					.findAny().orElseThrow(AssertionError::new).set(null, d);
		} catch (ReflectiveOperationException e) {
			log.error("Reflection failed. ", e);
		}
	}

	public enum Handler implements InvocationHandler {
		INSTANCE;

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			return null;
		}
	}
}
