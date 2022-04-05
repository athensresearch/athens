import styled, { css, keyframes } from 'styled-components';

const overlayAppear = keyframes`
  from {
    opacity: 0;
    transform: translateY(-10px);
  } to {
    opacity: 1;
    transform: translateY(0);
  }
`;

/**
 * A simple container with basic padding, background, shadow, etc.
 */
export const Overlay = styled.div`
  display: inline-flex;
  color: var(--body-text-color);
  padding: 0.25rem;
  min-width: 2em;
  border-radius: calc(0.25rem + 0.25rem); // Button corner radius + container padding makes "concentric" container radius;
  z-index: var(--zindex-dropdown);
  min-height: 2em;
  animation-fill-mode: both;
  box-shadow: var(--depth-shadow-16), 0 0 0 1px rgb(0 0 0 / 0.05);
  background: var(--background-plus-1);
  position: relative;

  &:focus-visible,
  &:focus {
    box-shadow: var(--depth-shadow-16), 0 0 0 2px rgb(0 0 0 / 0.1);
    outline: none;
  }

  &.animate-in {
    animation: ${overlayAppear} 0.125s;
  }
  
  ${props => !!props.hasOutline && css`
    .is-theme-dark & {
      &:after {
        content: '';
        inset: 0;
        position: absolute;
        box-shadow: inset 0 0 0 1px var(--body-text-color---opacity-lower);
        z-index: 99999;
        pointer-events: none;
        border-radius: inherit;
      }
    }
  `}
`;

Overlay.defaultProps = {
  hasOutline: true,
}
