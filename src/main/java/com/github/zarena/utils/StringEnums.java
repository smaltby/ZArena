package com.github.zarena.utils;

public enum StringEnums
{
	// Used in ZOPtionsButton
	VOTING_POPUP, ZOMBIE_TEXTURES, WAVE_COUNTER,
	// Used in ZTollSign & CommandHandler
	UO, USABLEONCE, OP, OPPOSITE, NR, NORESET,
	// Used in CommandHandler
	GENERAL, ZOMBIESPERWAVE, HEALTHPERWAVE, WAVE, SPAWNCHANCE, CHECKNEXTWAVE,
	// Used in DSpawnCommands & ISpawnCommands & ZSpawnCommands
	SET, JUMP,
	// Used in ZSpawnCommands
	ADD, REMOVE, DELETE, LIST, MARKBOSS,
	// Used in ZACommands
	ENTER, JOIN, LEAVE, RELOAD, STATS, VOTE, /*LIST,*/ NEW, CREATE, /*DELETE,*/ /*REMOVE,*/ SAVE, LOAD, GAMEMODE, ALIVE, SETALIVE, SETLEAVELOCATION,
	SETLEAVELOC, OPTIONS, SESSION, START, STOP, SETWAVE, INFO, /*WAVE,*/ DIA, DIAGNOSTIC,
	// Used in ZSignCommands
	MARKZSPAWN, MARK, /*RELOAD,*/
	// Used in WaveHandler
	QUADRATIC, LOGISTIC, LOGARITHMIC,
	// Used in ZArena
	ZOMBIE, SKELETON, WOLF, ZOMBIEPIGMAN,
	// Used in KCCommands
	TOP, /*SET,*/ SUB /*ADD*/;
}
