#!/bin/sh
java -Djava.library.path=app/bin/lwjgl-2.9.3/native/linux -classpath app/lib/app.jar:app/lib/RoguelikeGameKit.jar:app/lib/lwjgl.jar:app/lib/lwjgl_util.jar impcity.game.ImpCity

