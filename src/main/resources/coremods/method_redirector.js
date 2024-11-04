'use strict';

const ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');

// we can't run ASMAPI.loadData in the global context, so we do it here
// this function is called inside of initializeCoreMod
const replacements = [];
function initReplacements() {
    const targets = ASMAPI.loadData('coremods/finalize_spawn_targets.json');
    ASMAPI.log('DEBUG', 'Gathering Forge method redirector replacements');
    // ASMAPI.log('INFO', 'Got targets: {}', targets[1]);
    // ASMAPI.log('INFO', 'Got target class name: {}', targets[1].class);
    // ASMAPI.log('INFO', 'Got target class name: {}', targets[1].methods[0]);
    replacements.push({
        // finalizeSpawn redirection to ForgeEventFactory.onFinalizeSpawn
        'type': ASMAPI.MethodType.VIRTUAL,
        'name': 'finalizeSpawn',
        'desc': '(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/world/entity/SpawnGroupData;)Lnet/minecraft/world/entity/SpawnGroupData;',
        'targets': targets,
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
            ASMAPI.log('INFO', 'ADDING TARGET: {}', target.class);
            mergedTargets.push(target.class);
        }
    }

    return mergedTargets;
}

// const name = method.substring(0, method.indexOf('('));
// const desc = method.substring(method.indexOf('('));
function applyMethodRedirects(clazz) {
    // ASMAPI.log('INFO', 'Getting Forge method redirections for class {}', clazz.name);
    // ASMAPI.log('INFO', '{}', replacements[0].targets);
    // const classReplacements = replacements[0].targets.filter(t => contains(t, clazz.name));
    // ASMAPI.log('INFO', '{}', classReplacements);
    // if (classReplacements.length === 0) {
    //     reportNoTargetsError(clazz.name);
    //     return clazz;
    // }

    for (let replacement of replacements) {
        for (let methodString of getClassTargetMethods(clazz, replacement)) {
            ASMAPI.log('INFO', 'METHOD STRING = {}', methodString);
            const methodName = methodString.substring(0, methodString.indexOf('('));
            ASMAPI.log('INFO', 'METHOD NAME = {}', methodName);
            const methodDesc = methodString.substring(methodString.indexOf('('));
            ASMAPI.log('INFO', 'METHOD DESC = {}', methodDesc);
            const method = ASMAPI.findMethodNode(clazz, methodName, methodDesc);

            if (method == null) {
                ASMAPI.log('ERROR', 'Traget method {} not found in class {}!', methodString, clazz.name);
                continue;
            }

            for (let insn of method.instructions) {
                if (shouldReplace(insn, replacement)) {
                    const redirection = replacement.factory(insn);
                    ASMAPI.log('DEBUG', 'Redirecting method call {}{} to {}{} inside of {}.{}', insn.name, insn.desc, redirection.name, redirection.desc, clazz.name, method.name);
                    ASMAPI.insertInsn(method, insn, redirection, ASMAPI.InsertMode.REMOVE_ORIGINAL);
                }
            }
        }
    }

    return clazz;
}

function reportNoTargetsError(className) {
    ASMAPI.log('ERROR', 'No Forge method redirections found for class {}! This is a Forge bug, and is likely due to a Minecraft update changing something.', className);
}


/* HELPER FUNCTIONS FOR TARGET SEARCHING */

function shouldReplace(insn, replacement) {
    return insn.getOpcode() === replacement.type.toOpcode()
        && insn.name === replacement.name
        && insn.desc === replacement.desc;
}

function getClassTargetMethods(clazz, replacrement) {
    for (let t of replacrement.targets) {
        if (t.class === clazz.name) {
            return t.methods;
        }
    }
}

function contains(classTargets, target) {
    for (let s of classTargets) {
        ASMAPI.log('INFO', 'S = {}', s);
        if (s.class === target) return true;
    }

    return false;
}
