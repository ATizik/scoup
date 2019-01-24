package ru.atizik.scoup.fragments

import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment



//TODO:Document
interface ArgumentReceiver<T:Parcelable> {
    val argumentClazz: Class<T>
}

internal const val SCOUP_ARG = "__SCOUP_ARG"

fun <T,P:Parcelable> T.putArgs(argument: P):Bundle where T: Fragment, T:ArgumentReceiver<P> {
    return Bundle().apply {
        putParcelable(SCOUP_ARG,argument)
    }.also { arguments = it }
}