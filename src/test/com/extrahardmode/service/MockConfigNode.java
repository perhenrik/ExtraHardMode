package com.extrahardmode.service;

import com.extrahardmode.service.config.ConfigNode;

import java.util.Collections;

public enum MockConfigNode implements ConfigNode
{
    BOOL_FALSE      ("test01", VarType.BOOLEAN, false),
    BOOL_TRUE       ("test04", VarType.BOOLEAN, true),
    INT_0           ("test02", VarType.INTEGER, 0),
    INT_9           ("test03", VarType.INTEGER, 9),
    STR_0           ("test05", VarType.STRING, "derp"),

    NOTFOUND_BOOL   ("test06", VarType.BOOLEAN, true),
    NOTFOUND_INT    ("test08", VarType.INTEGER, 1),
    NOTFOUND_DOUBLE ("test10", VarType.DOUBLE, 0.0),
    NOTFOUND_STR    ("test07", VarType.STRING, "NaN"),
    NOTFOUND_LIST   ("test09", VarType.LIST, null),

    INHERITS_BOOL   ("test11", VarType.BOOLEAN, true),
    INHERITS_INT    ("test12", VarType.INTEGER, 42),
    INHERITS_DOUBLE ("test13", VarType.DOUBLE, 1.0),
    INHERITS_STR    ("test14", VarType.STRING, "test"),
    INHERITS_LIST   ("test15", VarType.LIST, null),

    /**
     * Integer Validation
     */
    INT_PERC_1      ("per01", VarType.INTEGER, SubType.PERCENTAGE, 100),

    INT_NN_1        ("nn01", VarType.INTEGER, SubType.NATURAL_NUMBER, 42),

    INT_Y_1         ("y01", VarType.INTEGER, SubType.Y_VALUE, 1),

    INT_HP_1        ("hp01", VarType.INTEGER, SubType.HEALTH, 5),
    ;

    String path;
    VarType type;
    SubType subType = null; //initialize because this is optional
    Object defaultValue;
    Disable disableValue;

    private MockConfigNode (String path, VarType type, Object defaultValue)
    {
        this.path = path;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    private MockConfigNode (String path, VarType type, SubType subType, Object defaultValue)
    {
        this(path, type, defaultValue);
        this.subType = subType;
    }

    @Override
    public String getPath ()
    {
        return path;
    }

    @Override
    public VarType getVarType ()
    {
        return type;
    }

    @Override
    public SubType getSubType ()
    {
        return subType;
    }

    @Override
    public Object getDefaultValue ()
    {
        return defaultValue;
    }

    /**
     * COPIED FROM ROOTNODE
     * Get the Object that will disable this option
     * @return Object that will disable this option in the plugin
     */
    @Override
    public Object getValueToDisable ()
    {
        Object obj;
        switch (type)
        {
            case BOOLEAN:
            {
                obj = false;
                break;
            }
            case INTEGER:
            {
                obj = 0;
                if (subType != null)
                {
                    switch (subType)
                    {
                        case NATURAL_NUMBER:
                        case Y_VALUE:
                        {
                            if (disableValue != null)
                                obj = (Integer) disableValue.get();
                            break;
                        }
                        case HEALTH:
                        {
                            obj = 20;
                            break;
                        }
                        case PERCENTAGE:
                        {
                            obj = 0;
                            break;
                        }
                        default:
                        {
                            obj = defaultValue;
                            throw new UnsupportedOperationException("SubType hasn't been specified for " + path);
                        }
                    }
                }
                break;
            }
            case DOUBLE:
            {
                obj = 0.0;
                break;
            }
            case STRING:
            {
                obj = "";
                break;
            }
            case LIST:
            {
                obj = Collections.emptyList();
                break;
            }
            default:
            {
                throw new UnsupportedOperationException("Type of " + type + " doesn't have a default value to be disabled");
            }
        }
        return obj;
    }


    /**
     * Contains values for some Nodes which require a special value, which differs from other Nodes with the same type
     */
    private enum Disable
    {
        /**
         * A value of 0 will disable this feature in the plugin
         */
        ZERO (0),
        /**
         * A value of 1 will disable this feature in the plugin
         */
        ONE (1);

        private Disable (Object obj)
        {
            disable = obj;
        }

        Object disable;

        public Object get()
        {
            return disable;
        }
    }
}
