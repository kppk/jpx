package kppk.jpx.module;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Reads module-info.java and parses it as {@link ModuleDescriptor}.
 * <p>
 * see https://docs.oracle.com/javase/specs/jls/se9/html/jls-7.html#jls-7.7
 */
public final class ModuleDescriptorReader {

    private final Reader reader;

    public ModuleDescriptorReader(Reader reader) {
        this.reader = reader;
    }

    public ModuleDescriptor read() throws IOException {
        Scanner scanner = new Scanner(reader);

        Scanner.Token token;
        ModuleDescriptorReader.BuilderContext context = new ModuleDescriptorReader.BuilderContext();
        ParserState state = ParserState.Start;
        while ((token = scanner.nextToken()) != Scanner.TOKEN_EOF) {
            state = state.next(token, context);
        }
        if (state != ParserState.End) {
            throw new IllegalArgumentException("Invalid module-info.java");
        }
        return context.moduleBuilder.build();
    }

    private static final class BuilderContext {
        ModuleDescriptor.Builder moduleBuilder;
        boolean isOpen;
        String tmpName;
        Set<String> tmpSet = new HashSet<>();
        List<String> tmpList = new ArrayList<>();
        Set<ModuleDescriptor.Requires.Modifier> reqMods = new HashSet<>();
    }

    private enum ParserState {

        Start {
            @Override
            public ParserState next(Scanner.Token token, BuilderContext ctx) {
                if (token.type == Scanner.TokenType.WORD) {
                    if ("module".equals(token.value)) {
                        return Module;
                    } else if ("open".equals(token.value)) {
                        ctx.isOpen = true;
                        return OpenModule;
                    }
                }
                throw new IllegalArgumentException("Unexpected token: " + token + ", expected open|module");
            }

        },

        OpenModule {
            @Override
            public ParserState next(Scanner.Token token, BuilderContext ctx) {
                if ("module".equals(token.value)) {
                    return Module;
                }
                throw new IllegalArgumentException("Unexpected token: " + token + ", expected module");
            }
        },

        Module {
            @Override
            public ParserState next(Scanner.Token token, BuilderContext ctx) {
                if (token.type == Scanner.TokenType.WORD) {
                    ctx.tmpName = token.value;
                    return ModuleName;
                }
                throw new IllegalArgumentException("Unexpected token: " + token + ", expected module name");
            }
        },

        ModuleName {
            @Override
            public ParserState next(Scanner.Token token, BuilderContext ctx) {
                if (token.type == Scanner.TokenType.CURLY_OPEN) {
                    ctx.moduleBuilder = ctx.isOpen ? ModuleDescriptor.newOpenModule(ctx.tmpName) : ModuleDescriptor.newModule(ctx.tmpName);
                    return ModuleDirectives;
                }
                throw new IllegalArgumentException("Unexpected token: " + token + ", expected curly open");
            }
        },

        ModuleDirectives {
            @Override
            public ParserState next(Scanner.Token token, BuilderContext ctx) {
                if (token.type == Scanner.TokenType.CURLY_CLOSE) {
                    // we are done
                    return End;
                } else if ("requires".equals(token.value)) {
                    return Requires;
                } else if ("exports".equals(token.value)) {
                    return Exports;
                } else if ("opens".equals(token.value)) {
                    return Opens;
                } else if ("uses".equals(token.value)) {
                    return Uses;
                } else if ("provides".equals(token.value)) {
                    return Provides;
                }
                throw new IllegalArgumentException("Unexpected token: " + token + ", expected requires|exports|opens|uses|provides");
            }

        },

        Requires {
            @Override
            public ParserState next(Scanner.Token token, BuilderContext ctx) {
                if ("transitive".equals(token.value)) {
                    ctx.reqMods.add(ModuleDescriptor.Requires.Modifier.TRANSITIVE);
                    return Requires;
                } else if ("static".equals(token.value)) {
                    ctx.reqMods.add(ModuleDescriptor.Requires.Modifier.STATIC);
                    return Requires;
                } else if (token.type == Scanner.TokenType.WORD) {
                    ctx.tmpName = token.value;
                    return Requires;
                } else if (token.type == Scanner.TokenType.SEMICOLON) {
                    ctx.moduleBuilder.requires(ctx.reqMods, ctx.tmpName);
                    ctx.reqMods.clear();
                    ctx.tmpName = null;
                    return ModuleDirectives;
                }
                throw new IllegalArgumentException("Unexpected token: " + token + ", expected requires|exports|opens|uses|provides");
            }
        },

        Exports {
            @Override
            public ParserState next(Scanner.Token token, BuilderContext ctx) {
                if (token.type == Scanner.TokenType.WORD) {
                    ctx.tmpName = token.value;
                    return ExportsName;
                }
                throw new IllegalArgumentException("Unexpected token: " + token + ", expected requires|exports|opens|uses|provides");
            }
        },

        ExportsName {
            @Override
            public ParserState next(Scanner.Token token, BuilderContext ctx) {
                if ("to".equals(token.value)) {
                    return ExportsTo;
                } else if (token.type == Scanner.TokenType.SEMICOLON) {
                    ctx.moduleBuilder.exports(ctx.tmpName);
                    ctx.tmpName = null;
                    return ModuleDirectives;
                }
                throw new IllegalArgumentException("Unexpected token: " + token + ", expected to|;");
            }
        },

        ExportsTo {
            @Override
            public ParserState next(Scanner.Token token, BuilderContext ctx) {
                if (token.type == Scanner.TokenType.WORD) {
                    ctx.tmpSet.add(token.value);
                    return ExportsTo;
                } else if (token.type == Scanner.TokenType.COMMA) {
                    return ExportsTo;
                } else if (token.type == Scanner.TokenType.SEMICOLON) {
                    ctx.moduleBuilder.exports(ctx.tmpName, ctx.tmpSet);
                    ctx.tmpName = null;
                    ctx.tmpSet.clear();
                    return ModuleDirectives;
                }
                throw new IllegalArgumentException("Unexpected token: " + token + ", expected ,|;|{name}");
            }
        },

        Opens {
            @Override
            public ParserState next(Scanner.Token token, BuilderContext ctx) {
                if (token.type == Scanner.TokenType.WORD) {
                    ctx.tmpName = token.value;
                    return OpensName;
                }
                throw new IllegalArgumentException("Unexpected token: " + token + ", expected requires|exports|opens|uses|provides");
            }
        },

        OpensName {
            @Override
            public ParserState next(Scanner.Token token, BuilderContext ctx) {
                if ("to".equals(token.value)) {
                    return OpensTo;
                } else if (token.type == Scanner.TokenType.SEMICOLON) {
                    ctx.moduleBuilder.opens(ctx.tmpName);
                    ctx.tmpName = null;
                    return ModuleDirectives;
                }
                throw new IllegalArgumentException("Unexpected token: " + token + ", expected to|;");
            }
        },

        OpensTo {
            @Override
            public ParserState next(Scanner.Token token, BuilderContext ctx) {
                if (token.type == Scanner.TokenType.WORD) {
                    ctx.tmpSet.add(token.value);
                    return OpensTo;
                } else if (token.type == Scanner.TokenType.COMMA) {
                    return OpensTo;
                } else if (token.type == Scanner.TokenType.SEMICOLON) {
                    ctx.moduleBuilder.opens(ctx.tmpName, ctx.tmpSet);
                    ctx.tmpName = null;
                    ctx.tmpSet.clear();
                    return ModuleDirectives;
                }
                throw new IllegalArgumentException("Unexpected token: " + token + ", expected ,|;|{name}");
            }
        },

        Uses {
            @Override
            public ParserState next(Scanner.Token token, BuilderContext ctx) {
                if (token.type == Scanner.TokenType.WORD) {
                    ctx.tmpName = token.value;
                    return Uses;
                } else if (token.type == Scanner.TokenType.SEMICOLON) {
                    ctx.moduleBuilder.uses(ctx.tmpName);
                    ctx.tmpName = null;
                    return ModuleDirectives;
                }
                throw new IllegalArgumentException("Unexpected token: " + token + ", expected {name}|;");
            }
        },

        Provides {
            @Override
            public ParserState next(Scanner.Token token, BuilderContext ctx) {
                if (token.type == Scanner.TokenType.WORD) {
                    ctx.tmpName = token.value;
                    return ProvidesName;
                }
                throw new IllegalArgumentException("Unexpected token: " + token + ", expected {name}");
            }
        },

        ProvidesName {
            @Override
            public ParserState next(Scanner.Token token, BuilderContext ctx) {
                if ("with".equals(token.value)) {
                    return ProvidesWith;
                }
                throw new IllegalArgumentException("Unexpected token: " + token + ", expected with");
            }
        },

        ProvidesWith {
            @Override
            public ParserState next(Scanner.Token token, BuilderContext ctx) {
                if (token.type == Scanner.TokenType.WORD) {
                    ctx.tmpList.add(token.value);
                    return ProvidesWith;
                } else if (token.type == Scanner.TokenType.COMMA) {
                    return ProvidesWith;
                } else if (token.type == Scanner.TokenType.SEMICOLON) {
                    ctx.moduleBuilder.provides(ctx.tmpName, ctx.tmpList);
                    ctx.tmpName = null;
                    ctx.tmpList.clear();
                    return ModuleDirectives;
                }
                throw new IllegalArgumentException("Unexpected token: " + token + ", expected ,|;|{name}");
            }
        },

        End {
            @Override
            public ParserState next(Scanner.Token token, BuilderContext ctx) {
                throw new IllegalArgumentException("Unexpected token: " + token + ", no more tokens expected");
            }
        };


        public abstract ParserState next(Scanner.Token token, BuilderContext ctx);
    }


}
