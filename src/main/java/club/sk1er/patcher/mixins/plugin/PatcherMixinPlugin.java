package club.sk1er.patcher.mixins.plugin;

import com.google.common.collect.ArrayListMultimap;
import kotlin.text.StringsKt;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class PatcherMixinPlugin implements IMixinConfigPlugin {
    private static final String LABYMOD_CLASS = "net/labymod/main/LabyMod.class";

    private static final ArrayListMultimap<String, String> CONFLICTING_CLASSES = ArrayListMultimap.create();

    static {
        CONFLICTING_CLASSES.put("GuiContainerMixin_MouseBindFixThatLabyBreaks", LABYMOD_CLASS);
    }

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        for (String conflictingClass : CONFLICTING_CLASSES.get(StringsKt.substringAfterLast(mixinClassName, '.', mixinClassName))) {
            if (this.getClass().getClassLoader().getResource(conflictingClass) != null) {
                // Conflicting class is present, let's not apply this
                return false;
            }
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
