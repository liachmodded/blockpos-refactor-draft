# BlockPos Refactoring
In one of the recent snapshots, I observed a large-scale refactor on `BlockPos`.
However, the changes failed to resolve one of the essential problems of `BlockPos`: immutability.

## What refactor
Before, the class hierarchy is like

`Vec3i` <- `BlockPos` <- `BlockPos.Mutable`

Now the class hierarchy will be 

`BlockPos`(interface) <- `ImmutableBlockPos` (`NewBlockPos.Immutable`), `MutableBlockPos` (`NewBlockPos.Mutable`)
                       
`Vec3i` <- `ImmutableBlockPos`

Usage refactor:
- `BlockPos` wherever the old block pos is used as a method parameter (input, etc)
- `ImmutableBlockPos` wherever the old block pos is used as return type to indicate immutability of the result
- `MutableBlockPos` wherever the old `BlockPos.Mutable` is used as return type (easy change)

The x,y,z fields in `Vec3i` will be final again, and `MutableBlockPos` will have 3 non-final x,y,z fields instead.

If there are cases where `BlockPos` or `MutableBlockPos` needs to be converted to `Vec3i`, simply add a `toImmutable` call.

It is encouraged for users to declare explicitly `ImmutableBlockPos` or `MutableBlockPos` in method bodies/local usage, as having a grasp on the type's immutability information is helpful.

## Why refactor
To guarantee the type-safety of immutable `BlockPos` through java inheritance.

When you obtain an `ImmutableBlockPos` or `Vec3i`, now you know the object is immutable, unlike previously, you always need to make a `toImmutable` call (and you cannot make the call on `Vec3i`, leading to possible pollutions)

For examples, maps tracking block positions will just use `ImmutableBlockPos` as keys to ensure the reliability of keys than having to remember to call `toImmutable` at every `put` call (now java compiler will ask you to call it), etc.

With this change, some builtin `BlockPos` methods can be more reliable. For example, `BlockPos.up()` is intended to return an immutable block pos. With this addition, `BlockPos.up()` forces returning an `ImmutableBlockPos`, the java compiler will just spit an error when you try to return a mutable block pos for `up` method, instead of having subtle bugs that are hardly noticed and looking around to find which methods to override and add a `toImmutable` call to the super result.

Since mutable block positions are mutable, they should not implement `Comparable` or override `equals`/`hashcode`.

## How refactor (In Intellij Idea)
1. Extract `BlockPos` methods for exposure to a new interface (don't make them abstract so the default impl will be shared by `MutableBlockPos` later)
1. Remove the super class of `BlockPos.Mutable` and make it implement the new interface. Add fields and override getters.
1. In what is to be `ImmutableBlockPos`, override `down` and `offset` methods to delegate either to `BlockPos.super` or `super` (Vec3i's impl).
1. Finally, change where `ImmutableBlockPos` is used as method parameters to `BlockPos`, and keep `ImmutableBlockPos` that are parts of method return types. Some other methods returning `ImmutableBlockPos` may have compile error because they are returning mutable block positions. Feel free to change return type to `BlockPos` or `MutableBlockPos` (either) in that case.

## Perf concern
By switching `BlockPos` to interface, a few method invocations will become `invokeinterface` than `invokevirtual` as before.

Even though there will be `invokeinterface` bytecode instructions, hotspot can optimize it easily as in most cases there is only two impls of `BlockPos`: `ImmutableBlockPos` and `MutableBlockPos`. As the game runs, hotspot can gradually optimize the method calls, and the performance difference will be little over time.

If you want to improve the performance of those defaulted inteface methods, you can override them in the two implementations instead (replace `getX` calls with field access/`toImmutable()` with `this` in immutable subclass)

To run jms benchmarks, edit the benchmarks in the `jmh` source set and execute `./gradlew jmh` for a result. The exemplary results of the current setup is available at [#1](https://github.com/liachmodded/blockpos-refactor-draft/issues/1)
