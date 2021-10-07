import React from 'react';
import ReactDOM from 'react-dom';
import styled from 'styled-components';
import { classnames } from '@/utils/classnames';
import { useFocusRing } from '@react-aria/focus'
import { useFocusRingEl } from '@/utils/useFocusRingEl';
import { mergeProps } from '@react-aria/utils';
import { DOMRoot } from '@/utils/config';

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
  shape?: 'rect' | 'round' | 'unset';
  /**
   * Button shape style. Set to 'unset' to manually style color and interaction styles.
   */
  variant?: 'plain' | 'gray' | 'tinted' | 'filled' | 'unset';
  /**
   * Styles provided to the button's focus ring.
   */
  focusRingStyle?: React.CSSProperties;
}


/**
 * Primary UI component for user interaction
 */
const ButtonWrap = styled.button.attrs<ButtonProps>(props => {
  if (props.isPrimary) props.variant = 'tinted';
  return ({
    "aria-pressed": props.isPressed ? 'true' : undefined,
    className: classnames(
      'button',
      props.className,
      'shape-' + props.shape,
      'variant-' + props.variant)
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
  background-color: transparent;
  transition-property: background, color;
  transition-duration: 0.075s;
  transition-timing-function: ease;
  gap: 0.5rem;
  text-align: left;

  &:focus {
    outline: none;
  }

  &:enabled {
    cursor: pointer;
  }

  span {
    flex: 1 0 auto;
  }

  /* Shapes */
  &.shape-rect {
    --padding-v: 0.375rem;
    --padding-h: 0.625rem;
    border-radius: 0.25rem;
    padding: var(--padding-v) var(--padding-h);
  }

  &.shape-round {
    --padding-v: 0.375rem;
    --padding-h: 0.625rem;
    border-radius: 2rem;
    padding: var(--padding-v) var(--padding-h);
  }

  svg:not(& * svg) {
    --icon-padding: 0.25rem;
    margin: calc((var(--padding-v) * -1) + var(--icon-padding)) calc((var(--padding-h) * -1) + var(--icon-padding));

    &:not(:first-child) {
      margin-left: 0.251em;
    }
    &:not(:last-child) {
      margin-right: 0.251em;
    }
  }

  /* Variants */
  &.variant-plain {
    background: transparent;

    &:hover {
      background: var(--body-text-color---opacity-05);
    }

    &[aria-pressed="true"],
    &:active {
      background: var(--body-text-color---opacity-10);
    }
  }

  &.variant-gray {
    color: var(--link-color);
    background: var(--body-text-color---opacity-10);

    &:hover {
      background: var(--body-text-color---opacity-15);
    }

    &[aria-pressed="true"],
    &:active {
      background: var(--body-text-color---opacity-20);
    }
  }

  &.variant-tinted {
    color: var(--link-color);
    background: var(--link-color---opacity-15);

    &:hover {
      background: var(--link-color---opacity-20);
    }

    &[aria-pressed="true"],
    &:active {
      background: var(--link-color---opacity-25);
    }
  }

  &.variant-filled {
    color: var(--link-color---contrast);
    background: var(--link-color);

    &:hover {
      background: var(--link-color---opacity-90);
    }

    &[aria-pressed="true"],
    &:active {
      background: var(--link-color---opacity-80);
    }
  }

  &:disabled {
    &,
    &:hover,
    &:active {
      color: var(--body-text-color---opacity-med);
      background: var(---body-text-color---opacity-10);
      cursor: not-allowed;
    }
  }
`;

interface Result extends React.ForwardRefExoticComponent<ButtonProps> {
  Wrap?: typeof ButtonWrap;
}

const _Button: Result = React.forwardRef((props: ButtonProps, ref): any => {
  ref = ref || React.useRef();
  let { FocusRing, focusProps } = useFocusRingEl(ref);

  return <>
    <ButtonWrap
      ref={ref}
      {...mergeProps(
        focusProps,
        props,
      )}>
      {props.children}
    </ButtonWrap>
    {FocusRing}
  </>;
});

_Button.defaultProps = {
  shape: 'rect',
  variant: 'plain',
}

export { _Button as Button };
