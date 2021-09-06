import React from 'react';
import styled from 'styled-components';
import { classnames } from '../../utils/classnames';

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  /**
   * Whether this button should have a stronger style
   */
  isPrimary?: boolean;
  /**
   * Whether the button should appear pressed
   */
  isPressed?: boolean;
  /**
   * Button shape. Set to 'unset' to manually style padding and radius.
   */
  shape: 'rect' | 'round' | 'unset';
  /**
   * Button shape style. Set to 'unset' to manually style color and interaction styles.
   */
  variant: 'plain' | 'gray' | 'tinted' | 'filled' | 'unset';
}


/**
 * Primary UI component for user interaction
 */
export const Button = styled.button.attrs<ButtonProps>(props => {
  const _shape = props.shape ? props.shape : 'rect';
  let _variant = props.variant ? props.variant : 'plain';
  if (props.isPrimary) _variant = 'tinted';
  return ({
    "aria-pressed": props.isPressed ? 'true' : 'false',
    className: classnames(
      'button',
      props.className,
      'shape-' + _shape,
      'variant-' + _variant)
  })
}) <ButtonProps>`
  margin: 0;
  font-family: inherit;
  font-size: inherit;
  font-weight: 500;
  border: none;
  display: inline-flex;
  place-items: center;
  place-content: center;
  color: var(--body-text-color);
  background-color: var(--background-color);
  transition-property: filter, backdrop-filter, background, color, opacity;
  transition-duration: 0.075s;
  transition-timing-function: ease;
  gap: 0.5rem;
  text-align: left;

  &:enabled {
    cursor: pointer;
  }

  span {
    flex: 1 0 auto;
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

  /* Shapes */
  &.shape-rect {
    border-radius: 0.25rem;
    padding: 0.375rem 0.625rem;
  }

  &.shape-round {
    border-radius: 2rem;
    padding: 0.375em 0.8rem;
  }

  /* Variants */
  &.variant-plain {
    background: transparent;
  }

  &.variant-gray {
    background: var(--body-text-color---opacity-lower);
  }

  &.variant-tinted {
    background: var(--link-color---opacity-lower);
    color: var(--link-color);
  }

  &.variant-filled {
    background: var(--link-color);
    color: var(--link-color---contrast, #fff);
  }

  /* States */
  &:enabled {
    cursor: pointer;

    &:hover {
      &.variant-filled {
        filter: brightness(95%);

        .is-theme-dark & {
          filter: brightness(110%);
        }
      }
      &.variant-plain,
      &.variant-gray,
      &.variant-tinted {
        backdrop-filter: brightness(95%) blur(2px);

        .is-theme-dark & {
          backdrop-filter: brightness(125%);
        }
      }
    }

    &:active,
    &[aria-pressed="true"] {

      &[class*="variant-"] {
        transition: filter 0s ease-in-out;
      }

      &.variant-filled {
        filter: brightness(90%);

        .is-theme-dark & {
          filter: brightness(120%);
        }
      }
      &.variant-plain,
      &.variant-gray,
      &.variant-tinted {
        backdrop-filter: brightness(90%) blur(2px);

        .is-theme-dark & {
          backdrop-filter: brightness(150%);
        }
      }
    }
  }
`;
