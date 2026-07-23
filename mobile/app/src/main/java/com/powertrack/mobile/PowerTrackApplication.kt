package com.powertrack.mobile

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** Punto de entrada de Hilt para todo el grafo de dependencias de la app. */
@HiltAndroidApp
class PowerTrackApplication : Application()
