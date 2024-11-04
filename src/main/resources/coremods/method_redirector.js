'use strict';

const ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');

// we can't run ASMAPI.loadData in the global context, so we do it here
// this function is called inside of initializeCoreMod
const replacements = [];
function initReplacements() {
    ASMAPI.log('DEBUG', 'Gathering Forge method redirector replacements');
    replacements.push({
        // finalizeSpawn redirection to ForgeEventFactory.onFinalizeSpawn
        'type': ASMAPI.MethodType.VIRTUAL,
        'name': 'finalizeSpawn',
        'desc': '(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/MobSpawnType;Lnet/minecraft/world/entity/SpawnGroupData;)Lnet/minecraft/world/entity/SpawnGroupData;',
        'targets': ASMAPI.loadData('coremods/finalize_spawn_targets.json'),
        'factory': function(insn) {
            return ASMAPI.buildMethodCall(
                ASMAPI.MethodType.STATIC,
                'net/minecraftforge/event/ForgeEventFactory',
                'onFinalizeSpawn',
                '(Lnet/minecraft/world/entity/Mob;Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/world/entity/SpawnGroupData;)Lnet/minecraft/world/entity/SpawnGroupData;');
        }
    });
}

function initializeCoreMod() {
    initReplacements();

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
        for (let target of replacement.targets) {
            mergedTargets.push(target);
        }
    }

    return mergedTargets;
}

function applyMethodRedirects(clazz) {
    for (let method of clazz.methods) {
        for (let insn of method.instructions) {
            const replacement = search(clazz.name, insn, replacements);
            if (replacement != null) {
                const redirection = replacement.factory(insn);
                ASMAPI.log('DEBUG', 'Redirecting method call {}{} to {}{} inside of {}.{}', insn.name, insn.desc, redirection.name, redirection.desc, clazz.name, method.name);
                ASMAPI.insertInsn(method, insn, redirection, ASMAPI.InsertMode.REMOVE_ORIGINAL);
            }
        }
    }

    return clazz;
}


/* HELPER FUNCTIONS FOR TARGET SEARCHING */

function search(className, insn, replacements) {
    for (let replacement of replacements){
        if (contains(replacement.targets, className)
            && insn.getOpcode() === replacement.type.toOpcode()
            && insn.name === replacement.name
            && insn.desc === replacement.desc) {
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
