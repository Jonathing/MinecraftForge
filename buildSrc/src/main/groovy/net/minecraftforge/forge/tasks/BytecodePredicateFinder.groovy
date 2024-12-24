package net.minecraftforge.forge.tasks


import groovy.transform.CompileStatic
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode

@CompileStatic
abstract class BytecodePredicateFinder extends BytecodeFinder {

    @Internal
    abstract Property<Closure<Boolean>> getPredicate()

    private final Map<String, Set<String>> matches = new HashMap<>()

    @Override
    protected process(ClassNode parent, MethodNode node) {
        for (final current : node.instructions) {
            if (predicate.get().call(parent, node, current)) {
                matches.compute(parent.name, { k, v -> v ?: new HashSet<>() }).add(node.name + node.desc)
                return
            }
        }
    }

    @Internal
    @Override
    protected Object getData() {
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