import styled from 'styled-components';
import { classnames } from '../../../utils/classnames';
import { SvgIcon } from '@material-ui/core';

const Wrapper = styled.div`
  display: flex;
  margin-left: 1rem;
  align-self: stretch;
  align-items: stretch;
  color: inherit;

  &.os-windows {
    button {
      border-radius: 0;
      width: 48px;
      min-height: 32px;
      display: flex;
      align-items: center;
      color: var(--body-text-color---opacity-med);
      background: var(--background-minus-1);
      transition: ackground 0.075s ease-in-out, filter 0.075s ease-in-out, color 0.075s ease-in-out;
      justify-content: center;
      border: 0;

      svg {
        path, line, rect {
          fill: none;
          stroke-width: 2px;
          stroke: currentColor;
        }
      }

      &:hover {
        backdrop-filter: brightness(92%);

        .theme-dark & {
          backdrop-filter: brightness(150%);
        }
      }

      &.close:hover {
        background: #e81123;
        filter: none;
        color: #fff;
      }
    }

    svg {
      font-size: 16px;
    }
  }

  &.os-linux {
    display: grid;
    padding: 4px;
    padding-right: 8px;
    grid-auto-flow: column;
    grid-gap: 4px;

    svg {
        path, line, rect {
          fill: none;
          stroke-width: 2px;
          stroke: currentColor;
        }
      }

    button {
      position: relative;
      margin: auto;
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      background: transparent;
      color: var(--body-text-color---opacity-med);
      transition: ackground 0.075s ease-in-out, filter 0.075s ease-in-out, color 0.075s ease-in-out;
      justify-content: center;
      border: 0;

      &:before {
        content: '';
        position: absolute;
        border-radius: 1000em;
        z-index: 0;
        background: var(--background-plus-1);
        inset: 6px;
      }

      svg {
        z-index: 1;
      }

      &.close {
        color: #fff;

        &:before {
          background: #555;
        }
      }
      &.minimize {
        svg {
          position: relative;
          top: 5px;
        }
      }

      svg {
        font-size: 12px;
      }

      .theme-light & {
        button:hover:before {
          backdrop-filter: brightness(92%);
        }
      }
      .theme-dark & {
        button:hover:before {
          backdrop-filter: brightness(150%);
        }
      }
    }

    .is-win-focused & {
      .close:before {
        background: #e9541f;
      };
    }
  }

`;

export interface WindowButtonsProps {

  handlePressMinimize(): void;
}

export const WindowButtons = ({
  os,
  isWinFocused,
  isWinFullscreen,
  isWinMaximized,
  handlePressMinimize,
  handlePressMaximizeRestore,
  handlePressClose
}) => {
  return (<Wrapper
    className={classnames(
      "os-" + os,
      isWinFocused && "is-win-focused",
      isWinFullscreen && "is-win-fullscreen",
      isWinMaximized && "is-win-maximized",
    )}
  >
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
