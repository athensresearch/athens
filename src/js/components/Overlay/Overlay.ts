import styled, { keyframes } from 'styled-components';

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
  
  &.animate-in {
    animation: ${overlayAppear} 0.125s;
  }
`;