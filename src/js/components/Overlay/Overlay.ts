import styled, { keyframes } from 'styled-components';

const dropdownAppear = keyframes`
  from {
    opacity: 0;
    transform: translateY(-10px);
  } to {
    opacity: 1;
    transform: translateY(0);
  }
`;

export const Overlay = styled.div`
  display: inline-flex;
  color: var(--body-text-color);
  padding: 0.25rem;
  min-width: 2em;
  border-radius: calc(0.25rem + 0.25rem); // Button corner radius + container padding makes "concentric" container radius;
  z-index: var(--zindex-dropdown);
  min-height: 2em;
  animation-fill-mode: both;
  /* box-shadow: [(:64 DEPTH-SHADOWS) ", 0 0 0 1px rgba(0, 0, 0, 0.05)"]]; */
  flex-direction: column;
  background: var(--background-plus-1);
  
  &.animate-in {
    animation: ${dropdownAppear} 0.125s;
  }
`;