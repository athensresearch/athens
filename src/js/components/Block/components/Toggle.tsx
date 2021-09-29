import React from 'react'
import styled from 'styled-components';
import { useFocusRing } from '@react-aria/focus';
import { mergeProps } from '@react-aria/utils';

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
  color: var(--body-text-color---opacity-low);

  &:focus {
    outline: none;
  }

  &:hover {
    color: var(--body-text-color---opacity-med);
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
    transition: transform 0.1s ease-in-out;
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

const FocusRing = styled.div`
  position: absolute;
  inset: 0.25rem -0.125rem;
  border: 2px solid var(--link-color);
  border-radius: 0.25rem;
`;

interface ToggleProps extends React.HTMLAttributes<HTMLButtonElement> {
  isOpen: boolean;
}

/**
 * Button to toggle the visibility of a block's child blocks.
 */
export const Toggle = React.forwardRef((props: ToggleProps, ref) => {
  console.log(props);
  const { isFocusVisible, focusProps } = useFocusRing();
  const {
    isOpen,
  } = props;

  return (
    <ToggleButton
      className={isOpen ? 'open' : 'closed'}
      ref={ref}
      {...mergeProps(focusProps, props)}
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
      {isFocusVisible && <FocusRing />}
    </ToggleButton>
  )
});
