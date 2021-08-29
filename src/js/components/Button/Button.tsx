import React from 'react';
import styled from 'styled-components';

const StyledButton = styled.button`
  cursor: pointer;
  padding: 0.375rem 0.625rem;
  margin: 0;
  font-family: inherit;
  font-size: inherit;
  border-radius: 0.25rem;
  font-weight: 500;
  border: none;
  display: inline-flex;
  align-items: center;
  color: var(--body-text-color);
  background-color: transparent;
  transition-property: filter, background, color, opacity;
  transition-duration: 0.075s;
  transition-timing-function: ease;
  gap: 0.5rem;

  &:hover {
    background: var(--body-text-color---opacity-lower);
  }

  &:active,
  &:hover:active,
  &[aria-pressed="true"] {
    color: var(--body-text-color);
    background: var(--body-text-color---opacity-lower);
  }

  &:active,
  &:hover:active,
  &:active[aria-pressed="true"] {
    background: var(--body-text-color---opacity-low);
  }

  &:disabled,
  &:disabled:active {
    color: var(--body-text-color---opacity-low);
    background: var(--body-text-color---opacity-lower);
    cursor: default;
  }

  span {
    flex: 1 0 auto;
    text-align: left;
  }

  kbd {
    margin-inline-start: 1rem;
    font-size: 85%;
  }

  > svg {
    margin: -0.0835em -0.325rem;

    &:not(:first-child) {
      margin-left: 0.251em;
    }
    &:not(:last-child) {
      margin-right: 0.251em;
    }
  }

  &.is-primary {
    color: var(--link-color);
    background: var(--link-color---opacity-lower);

    &:hover {
      background: var(--link-color---opacity-low);
    }

    &:active,
    &:hover:active,
    &[aria-pressed="true"] {
      color: white;
      background: var(--link-color);
    }

    &:disabled,
    &:disabled:active {
      color: var(--body-text-color---opacity-low);
      background: var(--body-text-color---opacity-lower);
      cursor: default;
    }
  }
`;

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  /**
   * Whether this button should have a stronger style
   */
  isPrimary?: boolean;
  /**
   * Whether the button should appear pressed
   */
  isPressed?: boolean;
}

/**
 * Primary UI component for user interaction
 */

export const Button = ({ isPrimary, isPressed, children, ...props }: ButtonProps) => {
  return (
    <StyledButton
      type="button"
      className={isPrimary ? 'is-primary' : ''}
      aria-pressed={isPressed}
      {...props}
    >
      {children}
    </StyledButton>
  );
};
