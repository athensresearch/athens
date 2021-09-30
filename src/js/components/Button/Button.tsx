import React from 'react';
import ReactDOM from 'react-dom';
import styled from 'styled-components';
import { classnames } from '@/utils/classnames';
import { useFocusRing } from '@react-aria/focus'
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
  shape: 'rect' | 'round' | 'unset';
  /**
   * Button shape style. Set to 'unset' to manually style color and interaction styles.
   */
  variant: 'plain' | 'gray' | 'tinted' | 'filled' | 'unset';
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

const FocusRing = styled.div`
  --inset: -3px;
  position: absolute;
  inset: -3px;
  border: 2px solid var(--link-color);
  top: calc(var(--top) + var(--inset));
  left: calc(var(--left) + var(--inset));
  width: calc(var(--width) - var(--inset) * 2);
  height: calc(var(--height) - var(--inset) * 2);
  z-index: 99999;

  &.shape-round {
    border-radius: 1000em;
  }

  &.shape-rect {
    border-radius: calc(0.25rem - var(--inset));
  }
`;

const focusRingRectStyle = (rectProps: DOMRect, otherProps) => {
  return {
    "--left": rectProps.left + 'px',
    "--top": rectProps.top + 'px',
    "--width": rectProps.width + 'px',
    "--height": rectProps.height + 'px',
    ...otherProps
  };
}

const _Button = React.forwardRef((props: ButtonProps, ref): any => {
  let { isFocusVisible, focusProps } = useFocusRing();
  ref = ref || React.useRef();
  let focusRingRect = ref.current?.getBoundingClientRect();

  return <>
    <ButtonWrap
      ref={ref}
      {...mergeProps(
        focusProps,
        props,
      )}>
      {props.children}
    </ButtonWrap>
    {isFocusVisible &&
      ReactDOM.createPortal(
        <FocusRing className={classnames(
          props.shape && 'shape-' + props.shape,
          props.variant && 'variant-' + props.variant,
        )}
          style={focusRingRectStyle(focusRingRect, props.focusRingStyle)}
        />, DOMRoot()
      )
    }
  </>;
});

_Button.defaultProps = {
  shape: 'rect',
  variant: 'plain',
}

export { _Button as Button };
