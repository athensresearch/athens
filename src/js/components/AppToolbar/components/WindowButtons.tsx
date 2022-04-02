import { Box } from '@chakra-ui/react';
import { SvgIcon } from '@material-ui/core';

const Wrapper = ({ children }) => <Box
  display="flex"
  marginLeft="1rem"
  alignSelf="stretch"
  alignItems="stretch"
  color="inherit"
  sx={{

    "button": {
      appearance: 'none',
    },

    ".os-windows &": {

      "button": {
        borderRadius: 0,
        width: "48px",
        minHeight: "32px",
        display: "flex",
        alignItems: "center",
        color: "foreground.secondary",
        background: "background.floor",
        transition: "background 0.075s ease-in-out, filter 0.075s ease-in-out, color 0.075s ease-in-out",
        justifyContent: "center",
        border: 0,

        "path, line, rect": {
          fill: "none",
          strokeWidth: "2px",
          stroke: "currentColor",
        },

        "&:hover, &:focus, &:focus-visible": {
          outline: "none",
          background: "background.upper",
        },

        "&.close:hover, &:focus, &.close:focus-visible": {
          background: "#e81123",
          filter: "none",
          color: "#fff",
        }
      },

      "svg": {
        fontSize: "16px",
      }
    },

    ".os-linux &": {
      display: "grid",
      padding: "4px",
      paddingRight: "8px",
      gridAutoFlow: "column",
      gridGap: "4px",

      "svg": {
        "path, line, rect": {
          fill: "none",
          strokeWidth: "2px",
          stroke: "currentColor",
        }
      },

      "button": {
        position: "relative",
        margin: "auto",
        width: "32px",
        height: "32px",
        display: "flex",
        alignItems: "center",
        background: "transparent",
        color: "foreground.secondary",
        transition: "background 0.075s ease-in-out, filter 0.075s ease-in-out, color 0.075s ease-in-out",
        justifyContent: "center",
        border: 0,

        "&:hover, &:focus, &:focus-visible": {
          outline: "none",
        },

        "&:before": {
          content: "''",
          position: "absolute",
          borderRadius: "full",
          zIndex: 0,
          background: "background.upper",
          inset: "6px",
        },

        "&:hover:before, &:focus-visible:before, &:focus:before": {
          background: "background.attic"
        },

        "svg": {
          zIndex: 1,
          fontSize: "12px",
        },

        "&.close": {
          color: "#fff",

          "&:before": {
            background: "#555",
          }
        },
        "&.minimize": {
          "svg": {
            position: "relative",
            top: "5px",
          }
        },
      },

      ".is-win-focused &": {
        ".close:before": {
          background: "#e9541f",
        },
      }
    }
  }}
>{children}</Box>;

export interface WindowButtonsProps {
  handlePressMinimize(): void,
}

export const WindowButtons = ({
  isWinFocused,
  isWinFullscreen,
  isWinMaximized,
  handlePressMinimize,
  handlePressMaximizeRestore,
  handlePressClose
}) => {
  return (<Wrapper>
    {/* Minimize button */}
    <button
      title="Minimize"
      onClick={handlePressMinimize}
      className="minimize"
    >
      <SvgIcon>
        <line x1={4} y1={11} x2={20} y2={11} />
      </SvgIcon>
    </button>
    {isWinFullscreen ? (
      <button
        title="Exit Fullscreen"
        onClick={handlePressMinimize}
        className="minimize"
      >
        <SvgIcon>
          <path d="M11 13L5 19M11 13V19M11 13H5" />
          <path d="M13 11L19.5 4.5M13 11L13 5M13 11L19 11" />
        </SvgIcon>
      </button>
    ) : (
      <button
        title={isWinMaximized ? "Restore" : "Maximize"}
        onClick={handlePressMaximizeRestore}
        className="maximize-restore"
      >
        {isWinMaximized ? (
          <SvgIcon>
            <path d="M11 13L5 19M11 13V19M11 13H5" />
            <path d="M13 11L19.5 4.5M13 11L13 5M13 11L19 11" />
          </SvgIcon>
        ) : (
          <SvgIcon>
            <rect height={14} width={14} x={5} y={5} />
          </SvgIcon>

        )}
      </button>
    )}
    <button
      title="Close Athens"
      onClick={handlePressClose}
      className="close"
    >
      <SvgIcon>
        <path d="M4 4L19 19M19 4L4 19" />
      </SvgIcon>
    </button>
  </Wrapper>)
}
