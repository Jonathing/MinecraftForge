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
        'desc': '(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/world/entity/SpawnGroupData;)Lnet/minecraft/world/entity/SpawnGroupData;',
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
    const classReplacements = replacements.filter(r => contains(r.targets, clazz.name));
    if (classReplacements.length === 0) {
        reportNoTargetsError(clazz.name);
        return clazz;
    }

    let applied = 0;
    for (let method of clazz.methods) {
        for (let insn of method.instructions) {
            const replacement = search(clazz.name, insn, classReplacements);
            if (replacement != null) {
                const redirection = replacement.factory(insn);
                ASMAPI.log('DEBUG', 'Redirecting method call {}{} to {}{} inside of {}.{}', insn.name, insn.desc, redirection.name, redirection.desc, clazz.name, method.name);
                ASMAPI.insertInsn(method, insn, redirection, ASMAPI.InsertMode.REMOVE_ORIGINAL);
                applied++;
            }
        }
    }

    if (applied === 0) {
        reportNoTargetsError(clazz.name);
        return clazz;
    }

    return clazz;
}

function reportNoTargetsError(className) {
    ASMAPI.log('ERROR', 'No Forge method redirections found for class {}! This is a Forge bug, and is likely due to a Minecraft update changing something.', className);
}


/* HELPER FUNCTIONS FOR TARGET SEARCHING */

function search(className, insn, classReplacements) {
    for (let replacement of classReplacements) {
        if (insn.getOpcode() === replacement.type.toOpcode()
            && insn.name === replacement.name
            && insn.desc === replacement.desc) {
            return replacement;
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
