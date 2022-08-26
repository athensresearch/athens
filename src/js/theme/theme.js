import { extendTheme, cssVar, Tooltip } from '@chakra-ui/react'
import { readableColor } from 'polished';
import { spacing } from './spacing'

const $arrowBg = cssVar("popper-arrow-bg");

const buttonIconFontSize = {
  xs: "16px",
  sm: "20px",
  md: "24px",
  lg: "32px",
}

const shadows = {
  focusLight: '0 0 0 3px #0071DB',
  focusDark: '0 0 0 3px #498eda',

  focusInsetLight: 'inset 0 0 0 3px #0071DB',
  focusInsetDark: 'inset 0 0 0 3px #498eda',

  page: '0 0.25rem 1rem #00000055',
}

const fonts = {
  body: '-apple-system,BlinkMacSystemFont,"Segoe UI",Roboto,Oxygen-Sans,Ubuntu,Cantarell,"Helvetica Neue",sans-serif',
  default: '-apple-system,BlinkMacSystemFont,"Segoe UI",Roboto,Oxygen-Sans,Ubuntu,Cantarell,"Helvetica Neue",sans-serif',
  code: 'ui-monospace, Menlo, Monaco, "Cascadia Mono", "Segoe UI Mono", "Roboto Mono", "Oxygen Mono", "Ubuntu Monospace", "Source Code Pro","Fira Mono", "Droid Sans Mono", "Courier New", monospace;',
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
      default: "0 0.125rem 0.5rem #00000055",
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
      default: '#00000033',
      _dark: '#ffffff22'
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
      default: '#fff',
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
      default: '#ffffffaa',
      _dark: '#222222aa'
    },
    // foreground colors
    "foreground.primary": {
      default: 'hsla(0, 0%, 0%, 0.87)',
      _dark: 'hsla(0, 0%, 100%, 0.83)'
    },
    "foreground.secondary": {
      default: 'hsla(0, 0%, 0%, 0.57)',
      _dark: 'hsla(0, 0%, 100%, 0.5)'
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
    infoText: {
      default: '#fff',
      _dark: '#fff'
    },
    warning: {
      default: '#D20000',
      _dark: '#DE3C21'
    },
    success: {
      default: '#4CBB17',
      _dark: '#498eda'
    },

    // Notifications
    notification: {
      default: '#d70015',
      _dark: '#ff6961'
    },
    notificationText: {
      default: '#fff',
      _dark: '#fff'
    },

    // other colors
    textHighlight: {
      default: '#ffdb8a',
      _dark: '#FBBE63'
    },
    "highlight": {
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

    gold: {
      default: '#F9A132',
      _dark: '#FBBE63'
    },
    goldContrast: {
      default: '#000',
      _dark: '#000'
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
      solid: ({ theme, status = "info" }) => {

        // Toasts don't recieve the current color mode
        // as a prop, so we get both colors and use one or
        // the other based on the CSS context
        const toastColorDefault = theme.semanticTokens.colors[status]?.default;
        const toastColorDark = theme.semanticTokens.colors[status]?.dark;

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
      },
      subtle: {
        container: {
          borderRadius: "md"
        },
        title: {
          fontWeight: "normal"
        },
      }
    }
  },
  Avatar: {
    baseStyle: (props) => {
      const { bg, color, size } = props;
      const exessLabelFontSize = theme.fontSizes[size || 'md'];

      return {
        container: {
          borderColor: "background.floor",
          ...makeAvatarColor(bg, color)
        },
        excessLabel: {
          background: "background.attic",
          fontSize: exessLabelFontSize,
          border: "2px solid transparent",
          backgroundClip: "padding-box",
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
      item: {
        overflow: 'hidden',
        ".fmt": {
          display: 'none',
        }
      },
    },
    variants: {
      strict: {
        link: {
          overflow: 'hidden !important',
          textOverflow: 'ellipsis !important',
          // Complicated selector applies to everything
          // except descendants of TODO checkboxes
          "*:not([data-checked] *):not([data-unchecked] *):not([class*='checkbox'])": {
            fontSize: 'inherit !important',
            fontWeight: 'inherit !important',
            lineHeight: 'inherit !important',
            fontFamily: 'inherit !important',
            color: 'inherit !important',
            background: 'none !important',
            textDecoration: 'none !important',
            display: "inline !important",
          }
        }
      }
    }
  },
  Button: {
    baseStyle: ({ size }) => ({
      transitionTimingFunction: 'ease-in-out',
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
      },
      "> .chakra-button__icon, > .chakra-icon, &.chakra-menu__menu-button > span > .chakra-icon": {
        fontSize: buttonIconFontSize[size],
      }
    }),
    variants: {
      link: {
        color: "link",
        borderRadius: "1px",
        _active: {
          color: "link",
          opacity: 0.8,
        }
      },
      solid: ({ colorScheme, colorMode }) => {

        const highlightColor = (colorMode === 'dark')
          ? theme.semanticTokens.colors["link"]._dark
          : theme.semanticTokens.colors["link"].default;

        if (colorScheme === 'highlight') {
          return ({
            color: highlightColor,
            bg: "interaction.surface",
            _hover: {
              bg: "interaction.surface.hover"
            },
            _active: {
              color: 'foreground.primary',
              bg: 'interaction.surface.active',
            },
          })
        } else {
          return ({
            bg: "interaction.surface",
            _hover: {
              bg: "interaction.surface.hover"
            },
            _active: {
              color: 'foreground.primary',
              bg: 'interaction.surface.active',
            },
          })
        }
      },
      ghost: ({ colorScheme }) => ({
        bg: "transparent",
        color: (colorScheme === 'subtle') ? "foreground.secondary" : "foreground.primary",
        _hover: {
          bg: "interaction.surface.hover"
        },
        _active: {
          color: 'foreground.primary',
          bg: 'interaction.surface.active',
        },
      }),
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
    },
  },
  Divider: {
    baseStyle: {
      borderColor: "separator.divider",
    }
  },
  FormLabel: {
    baseStyle: {
      fontSize: "sm",
      color: "foreground.secondary",
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
        fontSize: "0.75em",
        fontWeight: "700",
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
      }
    },
    sizes: {
      sm: {
        item: {
          padding: '0.35rem 0.5rem',
          lineHeight: '1.5',
          fontSize: "sm"
        },
        icon: {
          marginInlineEnd: "0.25rem",
        },
        groupTitle: {
          margin: '0.35rem 0.5rem',
        }
      }
    },
    defaultProps: {
      size: 'sm',
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
    parts: ["arrow", "content"],
    baseStyle: {
      content: {
        bg: "background.upper",
        border: "unset",
        shadow: "popover",
        _focus: {
          outline: 'none',
          shadow: "popover",
        },
        _focusVisible: {
          shadow: "popover",
        },
        [$arrowBg.variable]: "colors.background.upper",
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
            // Reset the default striped background so we can
            // set it more robustly below, including support
            // for virtualized tables.
            "&:nth-of-type(odd)": {
              td: {
                background: 'none'
              }
            },
            "&:nth-of-type(odd):not(.index-even), &.index-odd": {
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
    },
  },
}

// Default prop overrides
Tooltip.defaultProps = {
  ...Tooltip.defaultProps,
  closeOnMouseDown: true,
  openDelay: 500
}

const config = {
  initialColorMode: 'system',
  colorModeManager: 'localStorage',
}

const styles = {
  global: {
    'html, body': {
      bg: 'background.floor',
      color: 'foreground.primary',
      lineHeight: '1.5',
      height: '100vh',
      fontFamily: 'default',
      sx: {
        "::WebkitScrollbar": {
          background: "background.basement",
          width: "0.5rem",
          height: "0.5rem"
        },
        "::WebkitScrollbar-corner": { bg: "background.basement" },
        "::WebkitScrollbar-thumb": {
          bg: "background.upper",
          borderRadius: "full"
        }
      }
    },
    "#chakra-toast-manager-top-right, #chakra-toast-manager-top, #chakra-toast-manager-top-left": {
      margin: "3rem 1rem"
    },
    mark: {
      background: "gold",
      color: "goldContrast",
      padding: '0 0.2em',
      borderRadius: "sm",
    }
  }
}

const sizes = {
  ...spacing
}

export const theme = extendTheme({ config, radii, fonts, shadows, semanticTokens, spacing, sizes, components, styles });