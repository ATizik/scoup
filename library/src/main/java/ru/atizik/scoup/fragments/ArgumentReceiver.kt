package ru.atizik.scoup.fragments

import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment



/**
 * Usage: Inherit fragment from [ArgumentReceiver] interface, implement interface through delegation like this:
 * ArgumentReceiver<ArgumentType> by argRec()
 * Place argument into bundle by invoking [putArgs]
 * Now argument of type ArgumentType will be injected in Coordinator constructor
 */
interface ArgumentReceiver<T:Parcelable> {
    val argumentClazz: Class<T>
}

internal const val SCOUP_ARG = "__SCOUP_ARG"

fun <T,P:Parcelable> T.putArgs(argument: P):Bundle where T: Fragment, T:ArgumentReceiver<P> {
    return Bundle().apply {
        putParcelable(SCOUP_ARG,argument)
    }.also { arguments = it }
}

inline fun <reified T:Parcelable> argRec():ArgumentReceiver<T> = object:ArgumentReceiver<T> {
    override val argumentClazz: Class<T> = T::class.java
}