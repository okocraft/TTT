package net.okocraft.ttt.util;

import java.sql.SQLException;

public class Functions {

    @FunctionalInterface public interface TriFunction<A, B, C, R> { R apply(A a, B b, C c); }
    @FunctionalInterface public interface QuadFunction<A, B, C, D, R> { R apply(A a, B b, C c, D d); }
    @FunctionalInterface public interface SQLConsumer<A> { void accept(A a) throws SQLException; }
    @FunctionalInterface public interface SQLFunction<A, R> { R apply(A a) throws SQLException; }

}
