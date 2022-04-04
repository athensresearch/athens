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
  body: '"IBM Plex Serif", BlinkMacSystemFont,"Segoe UI Variable","Segoe UI",system-ui,ui-sans-serif,Helvetica,Arial,sans-serif,"Apple Color Emoji","Segoe UI Emoji"',
  default: '"IBM Plex Sans", BlinkMacSystemFont,"Segoe UI Variable","Segoe UI",system-ui,ui-sans-serif,Helvetica,Arial,sans-serif,"Apple Color Emoji","Segoe UI Emoji"',
  code: '"IBM Plex Mono", BlinkMacSystemFont, "Segoe UI Variable", "Segoe UI", system-ui, ui-sans-serif, Helvetica, Arial, sans-serif, "Apple Color Emoji", "Segoe UI Emoji"',
}

const radii = {
  none: '0',
  sm: '0.25rem',
  md: '0.5rem',
  lg: '1rem',
  full: '9999px',
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
      default: "0 0.25rem 1rem #00000022",
      _dark: "0 0.25rem 1rem #00000055",
    },
    menu: {
      default: "0 0.25rem 1rem #00000022",
      _dark: "0 0.25rem 1rem #00000022",
    },
    popover: {
      default: "0 0.25rem 1rem #00000055",
    },
    tooltip: {
      default: "0 0.25rem 1rem #00000055",
    },
    dialog: {
      default: "0 0.25rem 1rem #00000022",
    },
  },
  colors: {
    brand: {
      default: '#0071DB',
      _dark: '#498eda'
    },
    // separator colors
    "separator.border": {
      default: '00000022',
      _dark: '#ffffff55'
    },
    "separator.divider": {
      default: '#00000022',
      _dark: '#ffffff11'
    },
    // background colors
    "background.floor": {
      default: '#F6F6F6',
      _dark: '#1A1A1A'
    },
    "background.basement": {
      default: '#FAF8F6',
      _dark: '#111'
    },
    "background.upper": {
      default: '#fbfbfb',
      _dark: '#222'
    },
    "background.attic": {
      default: '#fff',
      _dark: '#333'
    },
    "background.vibrancy": {
      default: '#ffffff99',
      _dark: '#222222aa'
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
    "foreground.tertiary": {
      default: 'hsla(0, 0%, 0%, 0.2)',
      _dark: 'hsla(0, 0%, 100%, 0.2)'
    },
    // interactions
    "interaction.surface": {
      default: 'hsla(0, 0%, 0%, 0.04)',
      _dark: 'hsla(0, 0%, 100%, 0.04)',
    },
    "interaction.surface.hover": {
      default: 'hsla(0, 0%, 0%, 0.08)',
      _dark: 'hsla(0, 0%, 100%, 0.08)',
    },
    "interaction.surface.active": {
      default: 'hsla(0, 0%, 0%, 0.12)',
      _dark: 'hsla(0, 0%, 100%, 0.16)',
    },
    // status colors
    error: {
      default: '#D20000',
      _dark: '#DE3C21'
    },
    info: {
      default: '#0071DB',
      _dark: '#498eda'
    },
    warning: {
      default: '#D20000',
      _dark: '#DE3C21'
    },
    success: {
      default: '#4CBB17',
      _dark: '#498eda'
    },
    // other colors
    textHighlight: {
      default: '#ffdb8a',
      _dark: '#FBBE63'
    },
    highlight: {
      default: '#F9A132',
      _dark: '#FBBE63'
    },
    highlightContrast: {
      default: '#000',
      _dark: '#000'
    },
    link: {
      default: '#0071DB',
      _dark: '#498eda'
    },
    linkContrast: {
      default: '#fff',
      _dark: '#fff'
    },
    // block content colors
    "ref.foreground": {
      default: "#fbbe63bb",
      _dark: "#fbbe6366",
    },
    "ref.background": {
      default: "#fbbe63bb",
      _dark: "#fbbe6311",
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
      solid: ({ theme, status }) => {

        // Toasts don't recieve the current color mode
        // as a prop, so we get both colors and use one or
        // the other based on the CSS context
        const toastColorDefault = theme.semanticTokens.colors[ status ].default;
        const toastColorDark = theme.semanticTokens.colors[ status ].dark;

        return ({
          container: {
            bg: 'background.vibrancy',
            backdropFilter: "blur(20px)",
            color: "foreground.primary",
            "--toast-color": toastColorDefault,
            ".is-theme-dark &": {
              "--toast-color": toastColorDark,
            },
            _after: {
              content: "''",
              position: "absolute",
              inset: 0,
              borderRadius: "inherit",
              background: "var(--toast-color)",
              opacity: 0.1,
              zIndex: -1,
            }
          },
          title: {
            fontWeight: "normal"
          },
          icon: {
            color: "var(--toast-color)",
          }
        })
      }
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
        borderColor: "separator.border",
        _hover: {
          bg: "interaction.surface.hover",
        },
        _active: {
          bg: "interaction.surface.active"
        },
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
      container: {
        lineHeight: 1.5,
      },
      separator: {
        color: 'separator.border'
      },
    }
  },
  Button: {
    baseStyle: {
      transitionProperty: 'common',
      transitionTimingFunction: 'ease-in-out',
      "& > .chakra-button__icon svg": {
        height: "1.5em",
        width: "1.5em",
      },
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
        borderRadius: "1px"
      },
      solid: {
        bg: "interaction.surface",
        _hover: {
          bg: "interaction.surface.hover"
        },
        _active: {
          color: 'foreground.primary',
          bg: 'interaction.surface.active',
        },
      },
      ghost: {
        bg: "transparent",
        color: "foreground.primary",
        _hover: {
          bg: "interaction.surface.hover"
        },
        _active: {
          color: 'foreground.primary',
          bg: 'interaction.surface.active',
        },
      },
      outline: {
        bg: "transparent",
        borderWidth: "1px",
        borderStyle: "solid",
        borderColor: "interaction.surface.hover",
        _hover: {
          borderColor: "transparent",
          bg: "interaction.surface.hover",
        },
        _active: {
          color: 'foreground.primary',
          bg: 'interaction.surface.active',
          borderColor: "transparent",
        },
      }
    },
    colorSchemes: {
      error: {
        color: "error"
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
        p: 0,
        bg: 'background.vibrancy',
        borderColor: 'separator.divider',
        backdropFilter: "blur(20px)",
        minWidth: '0',
        width: 'max-content',
        shadow: 'menu'
      },
      groupTitle: {
        color: "foreground.secondary",
        textTransform: "uppercase",
        fontSize: "0.75em",
        fontWeight: "bold",
      },
      item: {
        "&.isActive": {
          bg: "background.attic",
        },
        "svg": {
          flexShrink: 0,
          fontSize: "1.5em",
        },
        _focus: {
          background: "interaction.surface.hover",
        },
        _focusVisible: {
          background: "interaction.surface.hover",
        },
        _hover: {
          background: "interaction.surface.hover",
        },
        _active: {
          background: "interaction.surface.active",
        },
        _expanded: {
          background: "interaction.surface.active",
        },
        // additional selector to catch icons not using the icon prop
        "&& > svg:first-of-type, svg:not(span > svg)": {
          marginInlineEnd: "0.75rem"
        },
      }
    },
    sizes: {
      sm: {
        item: {
          padding: '0.5rem 1rem',
          fontSize: "sm"
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
  Switch: {
    baseStyle: {
      track: {
        background: "foreground.tertiary",
        _checked: {
          background: "link",
        }
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
      lineHeight: '1.5',
      height: '100vh',
      fontFamily: 'default',
    },
    "#chakra-toast-manager-top-right, #chakra-toast-manager-top, #chakra-toast-manager-top-left": {
      margin: "3rem 1rem"
    }
  }
}

const sizes = {
  ...spacing
}

export const theme = extendTheme({ config, radii, fonts, shadows, semanticTokens, spacing, sizes, components, styles });
