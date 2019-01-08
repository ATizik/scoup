package ru.atizik.scoup.di

import toothpick.Scope
import toothpick.Toothpick
import toothpick.config.Binding
import toothpick.config.Module
import javax.inject.Provider
import kotlin.reflect.KClass


/**
 * bindClass: binds T to parameter(target)
 * bindInstance: binds T to instance created in parameter lambda
 *
 * For injection to actually work you need to complete these steps:
 * 1.@Inject annotation on constructor, or on field if it's a framework object(Activity, Service, etc.),
 * or init an annotated Provider for abstract class(e.g. Room db)
 * 2.Bind inside module(this step is sometimes optional)
 * 3.Attach module to scope. Modules attached to parent parentScopes are visible inside child parentScopes.
 * 4.Inject scope.
 *
 * Scopes have parent-child relationship, ordering of params from left to right is parent-child
 *
 *   val scope = Toothpick.openScope(this)
 *   scope.installModules(SomeModule())
 *   Toothpick.inject(this,scope)
 *
 * */

inline fun Scope.applyModules(vararg module: Module): Scope = apply { installModules(*module) }

inline fun <reified T> Module.bind(): Binding<T> = bind(T::class.java)

inline fun <reified T> Module.bindClass(target: () -> KClass<out Any>): Binding<T> =
    bind<T>().apply { to(target().java as Class<T>) }

inline fun <reified T> Module.bindClass(target: Class<out T>): Binding<T> =
    bind<T>().apply { to(target) }

inline fun <reified T> Module.bindClass(target: KClass<out Any>): Binding<T> =
    bind<T>().apply { to(target.java as Class<T>) }

inline fun <reified T> Module.bindInstance(target: () -> T): Binding<T> =
    bind<T>().apply { toInstance(target()) }

inline fun <reified T> Module.bindProvider(target: () -> Class<out Provider<T>>): Binding<T> =
    bind<T>().apply { toProvider(target()) }

inline fun <reified T> Module.bindProvider(target: KClass<out Provider<T>>): Binding<T> =
    bind<T>().apply { toProvider(target.java) }

inline fun <reified T> Module.bindProvider(target: Class<out Provider<T>>): Binding<T> =
    bind<T>().apply { toProvider(target) }

inline fun <reified T> Module.bindProviderInstance(target: Provider<T>): Binding<T> =
    bind<T>().apply { toProviderInstance(target) }

inline fun <reified T> Module.bindProviderInstance(noinline target: () -> T): Binding<T> =
    bind<T>().apply {
        toProviderInstance(target.asProvider())
    }

fun <T> (() -> T).asProvider(): Provider<T> {
    return Provider { invoke() }
}

fun module(bindings: Module.() -> Binding<*>?): Module = Module().apply { bindings() }

fun simpleScope(scopeName: Any, bindings: Module.() -> Binding<*>?): Scope =
    Toothpick.openScope(scopeName).apply { installTestModules(Module().apply { bindings() }) }

fun scope(scopeName: Any, vararg bindings: Scope.() -> Module?): Scope =
    Toothpick.openScope(scopeName).apply { bindings.forEach { installModules(it()) } }