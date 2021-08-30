import styled from 'styled-components';

export const ToggleButton = styled.button`
  width: 1em;
  grid-area: toggle;
  height: 2em;
  position: relative;
  z-index: 2;
  flex-shrink: 0;
  display: flex;
  background: none;
  border: none;
  transition: color 0.05s ease;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: inherit;
  padding: 0;
  -webkit-appearance: none;

  &:hover {
    color: var(--link-color);
  }

  &:before {
    content: '';
    inset: 0.25rem -0.125rem;
    z-index: -1;
    position: absolute;
    transition: opacity 0.1s ease;
    border-radius: 0.25rem;
    opacity: 0;
    background: var(--background-plus-2);
    box-shadow: var(--depth-shadow-8);
  }

  &:hover:before,
  &:focus-visible:before {
    opacity: 1;
  }

  svg {
    transform: rotate(90deg);
    vector-effect: non-scaling-stroke;
  }

  &.closed {
    svg {
      transform: rotate(0deg);
    };
  }

  &:empty {
    pointer-events: none;
  }
`;

export const Toggle = ({ isOpen, handlePressToggle, linkedRef, uid, ...props }) => (
  <ToggleButton
    className={isOpen ? 'open' : 'closed'}
    tabIndex={0}
    onClick={(e) => {
      e.stopPropagation();
      handlePressToggle();
      console.log('toggled block open/closed')
    }}
    {...props}
  >
    <svg
      width="24"
      height="24"
      viewBox="0 0 24 24"
      xmlns="http://www.w3.org/2000/svg"
    >
      <path d="M10 6L16 11.5L10 17"
        fill="none"
        stroke="currentColor"
        strokeWidth={3}
        strokeLinecap="round"
      />
    </svg>
  </ToggleButton>
);
