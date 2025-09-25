package net.minecraftforge.forge.build.tasks

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode

@CompileStatic
abstract class BytecodePredicateFinder extends BytecodeFinder {
    private final transient Map<String, List<String>> matches = new TreeMap<>(Comparator.naturalOrder())

    protected abstract @Internal Property<Closure<Boolean>> getPredicate()

    @Override
    protected process(ClassNode parent, MethodNode node) {
        var predicate = this.predicate.get()
        for (final current : node.instructions) {
            if (predicate.call(parent, node, current)) {
                var methods = matches.compute(parent.name, { k, v -> v ?: new ArrayList<>() })

                // only add non-synthetic methods
                if ((node.access & Opcodes.ACC_SYNTHETIC) === 0) {
                    methods.add(node.name + node.desc)
                }

                return
            }
        }
    }

    @Override
    @PackageScope @Internal Object getData() {
        var array = new ArrayList<Object>()
        matches.forEach { c, m ->
            {
                array.add(
                        'class': c,
                        'methods': m
                )
            }
        }
        return array ?: { throw new RuntimeException('Failed to find any targets, please ensure that method names and descriptors are correct.') }()
    }
}