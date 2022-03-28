import { extendTheme, cssVar } from '@chakra-ui/react'
import { readableColor } from 'polished';
import { spacing } from './spacing'

const $arrowBg = cssVar("popper-arrow-bg");

const shadows = {
  focusLight: '0 0 0 3px #0071DB',
  focusDark: '0 0 0 3px #498eda',

  focusInsetLight: 'inset 0 0 0 3px #0071DB',
  focusInsetDark: 'inset 0 0 0 3px #498eda',

  page: '0 0.25rem 1rem #00000055',
}

const fonts = {
  code: '"SFMono-Regular", Consolas, "Liberation Mono", Menlo, Courier, monospace',
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
    danger: {
      default: 'warningLight',
      _dark: 'warningDark'
    },
    info: {
      default: 'linkLight',
      _dark: 'linkDark'
    },
    warning: {
      default: 'warningLight',
      _dark: 'warningDark'
    },
    success: {
      default: 'confirmationLight',
      _dark: 'confirmationDark'
    },
    // other colors
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
    },
    // block content colors
    "ref.feature": {
      default: "#fbbe6322",
      _dark: "#fbbe6322",
    }
  }
}

const makeAvatarColor = (bg, color) => {
  if (bg && color) {
    return {
      col: bg,
      color: color,
    }
  } else if (bg && !color) {
    return {
      bg: bg,
      color: readableColor(bg)
    }
  } else if (color && !bg) {
    return {
      bg: readableColor(color),
      color: color,
    }
  }
}

const components = {
  Alert: {
    variants: {
      // variant used by toasts
      solid: ({ colorScheme }) => ({
        container: {
          bg: 'background.vibrancy',
          backdropFilter: "blur(20px)",
          color: "foreground.primary",
          _after: {
            content: "''",
            position: "absolute",
            inset: 0,
            borderRadius: "inherit",
            background: colorScheme,
            opacity: 0.1,
            zIndex: -1,
          }
        },
        title: {
          fontWeight: "normal"
        },
        icon: {
          color: colorScheme || "info"
        }
      })
    }
  },
  Avatar: {
    baseStyle: ({ bg, color }) => {
      return {
        container: {
          borderColor: "background.floor",
          ...makeAvatarColor(bg, color)
        },
      }
    },
  },
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
      },
    }
  },
  Breadcrumb: {
    baseStyle: {
      separator: {
        color: 'separator.border'
      },
    }
  },
  Button: {
    baseStyle: {
      color: 'foreground.primary',
      _active: {
        transitionDuration: "0s",
      },
      _focus: {
        outline: 'none',
        boxShadow: 'none'
      },
      _focusVisible: {
        outline: 'none',
        boxShadow: 'focus'
      }
    },
    variants: {
      link: {
        color: "link",
        borderRadius: "unset"
      },
      solid: {
        _active: {
          color: 'foreground.primary',
          bg: 'background.attic',
        },
      }
    },
    colorSchemes: {
      danger: {
        color: "danger"
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
        transitionDuration: "0s",
      },
      _focus: {
        outline: 'none',
        boxShadow: 'none'
      },
      _focusVisible: {
        outline: 'none',
        boxShadow: 'focus'
      }
    },
    variants: {
      solid: {
        _active: {
          color: 'linkContrast',
          bg: 'link',
        },
      }
    }
  },
  Menu: {
    baseStyle: {
      list: {
        zIndex: 3,
        overflow: 'hidden',
        py: 0,
        bg: 'background.upper',
        shadow: 'menu'
      },
      groupTitle: {
        color: "foreground.secondary",
        textTransform: "uppercase",
        fontSize: "0.75em",
        fontWeight: "bold",
      }
    },
    sizes: {
      sm: {
        item: {
          padding: '0.5rem 1rem',
        }
      }
    }
  },
  Modal: {
    baseStyle: {
      dialogContainer: {
        WebkitAppRegion: 'no-drag',
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
  Spinner: {
    baseStyle: ({ thickness }) => ({
      flexShrink: 0,
      color: "separator.border",
      borderWidth: thickness,
    }),
    defaultProps: {
      thickness: '1.5px',
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

export const theme = extendTheme({ config, radii, fonts, colors, shadows, semanticTokens, spacing, sizes, components, styles });
