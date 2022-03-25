import { extendTheme, cssVar } from '@chakra-ui/react'
import { spacing } from './spacing'

const $arrowBg = cssVar("popper-arrow-bg");

const shadows = {
  focusLight: '0 0 0 3px #0071DB',
  focusDark: '0 0 0 3px #498eda',

  focusInsetLight: 'inset 0 0 0 3px #0071DB',
  focusInsetDark: 'inset 0 0 0 3px #498eda',

  page: '0 0.25rem 1rem #00000055',
}

const radii = {
  none: '0',
  sm: '0.25rem',
  md: '0.5rem',
  lg: '1rem',
  full: '9999px',
}

const colors = {
  // light theme
  linkLight: "#0071DB",
  linkContrastLight: '#fff',
  highlightLight: "#F9A132",
  textHighlightLight: "#ffdb8a",
  highlightContrastLight: "#000",
  warningLight: "#D20000",

  backgroundPlus2Light: "#fff",
  backgroundPlus1Light: "#fbfbfb",
  backgroundColorLight: "#F6F6F6",
  backgroundMinus1Light: "#FAF8F6",
  backgroundMinus2Light: "#EFEDEB",

  backgroundVibrancyLight: "#ffffff55",

  confirmationLight: "#009E23",
  headerLight: "#322F38",
  bodyLight: "#433F38",
  borderLight: "hsla(32, 81%, 10:  0.08)",
  errorLight: "#fd5243",
  shadowLight: "#000",

  // dark theme
  backgroundMinu2Dark: "#151515",
  backgroundMinus1Dark: "#111",
  backgroundColorDark: "#1A1A1A",
  backgroundPlus1Dark: "#222",
  backgroundPlus2Dark: "#333",

  backgroundVibrancyDark: "#222222cc",

  linkDark: "#498eda",
  linkContrastDark: '#fff',
  highlightDark: "#FBBE63",
  textHighlightDark: "#FBBE63",
  highlightContrastDark: "#000",
  warningDark: "#DE3C21",

  confirmationDark: "#189E36",
  headerDark: "#BABABA",
  bodyDark: "#AAA",
  borderDark: "hsla(32, 81%, 90%, 0.08)",
  errorDark: "#fd5243",
  shadowDark: "#000",
}

const semanticTokens = {
  shadows: {
    focus: {
      default: "focusLight",
      _dark: "focusDark",
    },
    focusInset: {
      default: "focusInsetLight",
      _dark: "focusInsetDark",
    },
    focusPlaceholder: {
      default: '0 0 0 3px transparent'
    },
    focusPlaceholderInset: {
      default: 'inset 0 0 0 3px transparent'
    },
    page: {
      default: "0 0.25rem 1rem #00000055",
    },
    menu: {
      default: "0 0.25rem 1rem #00000055",
    },
    popover: {
      default: "0 0.25rem 1rem #00000055",
    },
    tooltip: {
      default: "0 0.25rem 1rem #00000055",
    },
    dialog: {
      default: "0 0.25rem 1rem #00000055",
    },
  },
  colors: {
    transparent: 'transparent',
    brand: {
      default: 'linkLight',
      _dark: 'linkDark'
    },
    // separator colors
    "separator.border": {
      default: '00000011',
      _dark: '#ffffff55'
    },
    "separator.divider": {
      default: '#00000011',
      _dark: '#ffffff22'
    },
    // background colors
    "background.floor": {
      default: 'backgroundColorLight',
      _dark: 'backgroundColorDark'
    },
    "background.basement": {
      default: 'backgroundMinus1Light',
      _dark: 'backgroundMinus1Dark'
    },
    "background.upper": {
      default: 'backgroundPlus1Light',
      _dark: 'backgroundPlus1Dark'
    },
    "background.attic": {
      default: 'backgroundPlus2Light',
      _dark: 'backgroundPlus2Dark'
    },
    "background.vibrancy": {
      default: 'backgroundVibrancyLight',
      _dark: 'backgroundVibrancyDark'
    },
    // foreground colors
    "foreground.primary": {
      default: 'hsla(0, 0%, 0%, 0.87)',
      _dark: 'hsla(0, 0%, 100%, 0.8)'
    },
    "foreground.secondary": {
      default: 'hsla(0, 0%, 0%, 0.57)',
      _dark: 'hsla(0, 0%, 100%, 0.57)'
    },
    // status colors
    error: {
      default: 'errorLight',
      _dark: 'errorDark'
    },
    warning: {
      default: 'warningLight',
      _dark: 'warningDark'
    },
    confirmation: {
      default: 'confirmationLight',
      _dark: 'confirmationDark'
    },
    // other colors
    headerText: {
      default: 'headerLight',
      _dark: 'headerDark'
    },
    textHighlight: {
      default: 'textHighlightLight',
      _dark: 'textHighlightDark'
    },
    highlight: {
      default: 'highlightLight',
      _dark: 'highlightDark'
    },
    highlightContrast: {
      default: 'highlightContrastLight',
      _dark: 'highlightContrastDark'
    },
    link: {
      default: 'linkLight',
      _dark: 'linkDark'
    },
    linkContrast: {
      default: 'linkContrastLight',
      _dark: 'linkContrastDark'
    }
  }
}

const components = {
  Accordion: {
    baseStyle: {
      button: {
        borderRadius: "sm",
        _focus: {
          outline: "none",
          boxShadow: "none",
        },
        _focusVisible: {
          outline: "none",
          boxShadow: "focusInset"
        }
      }
    }
  },
  Breadcrumb: {
    baseStyle: {
      separator: {
        color: 'separator.border'
      }
    }
  },
  Button: {
    baseStyle: {
      color: 'foreground.primary',
      _active: {
        transitionDuration: '0s',
        color: 'linkContrast',
        bg: 'link',
      },
      _focus: {
        outline: 'none',
        boxShadow: 'none'
      },
      _focusVisible: {
        outline: 'none',
        boxShadow: 'focus'
      }
    }
  },
  FormLabel: {
    baseStyle: {
      color: "foreground.secondary",
    }
  },
  IconButton: {
    baseStyle: {
      fontSize: "1em",
      _active: {
        transitionDuration: '0s',
        color: 'linkContrast',
        bg: 'link',
      },
      _focus: {
        outline: 'none',
        boxShadow: 'none'
      },
      _focusVisible: {
        outline: 'none',
        boxShadow: 'focus'
      }
    }
  },
  Menu: {
    baseStyle: {
      list: {
        zIndex: 2,
        bg: 'background.upper',
        shadow: 'menu'
      }
    }
  },
  Modal: {
    baseStyle: {
      dialogContainer: {
        _focus: {
          outline: 'none'
        }
      },
      dialog: {
        shadow: "dialog",
        border: "1px solid",
        borderColor: 'separator.divider',
        bg: 'background.upper'
      }
    }
  },
  Popover: {
    parts: [ "arrow", "content" ],
    baseStyle: {
      content: {
        bg: "background.upper",
        shadow: "popover",
        [ $arrowBg.variable ]: "colors.background.upper",
      }
    }
  },
  Table: {
    baseStyle: {
      border: 'separator.divider',
    },
    variants: {
      striped: {
        td: {
          border: 'none',
        },
        thead: {
          th: {
            border: 'none'
          }
        },
        tbody: {
          tr: {
            "&:nth-of-type(odd)": {
              td: {
                background: "background.upper",
                "&:first-of-type": {
                  borderLeftRadius: "lg"
                },
                "&:last-of-type": {
                  borderRightRadius: "lg"
                }
              },
            },
          },
        },
      }
    }
  },
  Tabs: {
    variants: {
      line: {
        tabList: {
          borderBottom: "separator.border"
        },
        tab: {
          color: "link",
          borderBottom: "2px solid",
          borderBottomColor: "separator.border",
          _selected: {
            bg: 'background.attic',
            color: 'foreground.primary',
            borderBottomColor: "foreground.primary"
          },
          _focus: {
            outline: 'none',
            shadow: 'none'
          },
          _focusVisible: {
            shadow: "focus"
          }
        }
      }
    }
  },
  Tooltip: {
    baseStyle: {
      bg: 'background.attic',
      color: 'foreground.primary',
      shadow: 'tooltip',
      px: "8px",
      py: "2px",
      borderRadius: "sm",
      fontWeight: "medium",
      fontSize: "sm",
      boxShadow: "md",
      maxW: "320px",
      zIndex: "tooltip",
    }
  },
}

const config = {
  initialColorMode: 'dark',
  useSystemColorMode: true,
}

const styles = {
  global: {
    'html, body': {
      bg: 'background.floor',
      color: 'foreground.primary',
    }
  }
}

const sizes = {
  ...spacing
}

export const theme = extendTheme({ config, radii, colors, shadows, semanticTokens, spacing, sizes, components, styles });