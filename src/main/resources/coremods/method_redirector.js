'use strict'; // prevents us from making sloppy mistakes

var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');

const replacements = [
    {
        // finalizeSpawn redirection to ForgeEventFactory.onFinalizeSpawn
        'opcode': ASMAPI.MethodType.VIRTUAL.toOpcode(),
        'name': 'finalizeSpawn',
        'desc': '(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/MobSpawnType;Lnet/minecraft/world/entity/SpawnGroupData;)Lnet/minecraft/world/entity/SpawnGroupData;',
        'targets': 'coremods/finalize_spawn_targets.json',
        'factory': function(insn) {
            return ASMAPI.buildMethodCall(
                ASMAPI.MethodType.STATIC,
                "net/minecraftforge/event/ForgeEventFactory",
                "onFinalizeSpawn",
                "(Lnet/minecraft/world/entity/Mob;Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/world/entity/SpawnGroupData;)Lnet/minecraft/world/entity/SpawnGroupData;");
        }
    }
];

function initializeCoreMod() {
    return {
        'forge_method_redirector': {
            'target': {
                'type': 'CLASS',
                'names': getTargets
            },
            'transformer': applyMethodRedirects
        }
    }
}

function getTargets(classes) {
    const mergedTargets = [];
    for (let replacement of replacements) {
        replacement.targets = ASMAPI.loadData(replacement.targets);
        for (let target of r.targets) {
            mergedTargets.push(r.targets[k]);
        }
    }

    return mergedTargets;
}

function applyMethodRedirects(clazz) {
    for (let method of clazz.methods) {
        for (let insn of method.instructions) {
            const replacement = search(clazz.name, insn, replacements);
            if (replacement != null) {
                instr.set(insn, temp.factory(insn));
            }
        }
    }

    return clazz;
}


/* HELPER FUNCTIONS FOR TARGET SEARCHING */

function search(className, node, replacements) {
    for (let replacement of replacements){
        if (contains(replacement.targets, className)
            && node.getOpcode() === replacement.opcode
            && node.name === replacement.name
            && node.desc === replacement.desc) {
            return r;
        }
    }
    return null;
}

function contains(list, target) {
    for (let s of list) {
        if (s === target) return true;
    }

    return false;
}
