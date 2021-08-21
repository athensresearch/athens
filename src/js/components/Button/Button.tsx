import React from 'react';
import styled from 'styled-components';

const StyledButton = styled.button`
  font-family: 'IBM Plex Sans';
  font-weight: 500;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  --font-size: 1em;
  --min-height: 1.5em;
  min-height: var(--min-height);
  border: var(--border);
  border-top-left-radius: var(--border-radius);
  border-bottom-left-radius: var(--border-radius);
  border-top-right-radius: var(--border-radius);
  border-bottom-right-radius: var(--border-radius);
  
  &:hover:not([disabled]) {
    filter: contrast(110%);
  }

  &.border-none {
    border: 0;
  }
 
  &.shape-rect {
    --border-radius: 0;
  }
  &.shape-round {
    --border-radius: 0.2rem;
    --border-radius: calc(var(--min-height) / 2);
  }
  &.shape-capsule {
    --border-radius: 1000em;
  }

  &.primary {
    color: #fff;
    background: var(--link-color);
  }
  &.secondary {
    color: var(--link-color);
  }

  &.size-small {
    font-size: var(--font-size, 12px);
    padding: var(--padding, 10px 16px);
  }

  &.size-medium {
    font-size: var(--font-size, 14px);
    padding: var(--padding, 11px 20px);
  }

  &.size-large {
    font-size: var(--font-size, 16px);
    padding: var(--padding, 12px 24px);
  }
`;

export interface ButtonProps {
  /**
   * Is this the principal call to action on the page?
   */
  primary: boolean | undefined;
  /**
   * How large should the button be?
   */
  size: 'small' | 'medium' | 'large' | undefined;
  /**
   * Button shape
   */
  shape: 'capsule' | 'round' | 'rect' | undefined;
  /**
   * Button border
   */
  border: 'none' | undefined;
  icon: JSX.Element | undefined;
}

/**
 * Primary UI component for user interaction
 */
export const Button: React.FC<ButtonProps> = ({
  children,
  primary = false, 
  shape = 'rect',
  size = 'small',
  border = 'none',
  icon = null,
  ...props
}) => {
  return (
    <StyledButton
      type="button"
      className={[
        'button',
        primary ? 'primary' : 'secondary',
        size && `size-${size}`,
        shape && `shape-${shape}`,
        border && `border-${border}`,
      ].join(' ')}
      {...props}
    >
      {icon && icon}
      {children}
    </StyledButton>
  );
};
