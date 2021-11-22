import React from 'react';
import styled from 'styled-components';
import { useSwitch } from '@react-aria/switch'
import { useToggleState } from '@react-stately/toggle';
import { HoverProps, useHover } from '@react-aria/interactions';
import { useFocusRing } from '@react-aria/focus';
import { usePress } from '@react-aria/interactions'
import { useVisuallyHidden } from '@react-aria/visually-hidden'
import { AriaSwitchProps } from '@react-types/switch';
import { mergeProps } from '@react-aria/utils';

import { classnames } from '@/utils/classnames';

const Handle = styled.rect``;

const Track = styled.rect``;

const FocusRing = styled.rect``;

const Svg = styled.svg`
  height: 1em;
  flex: 0 0 auto;
  overflow: visible;
  &,
  & * {
    transform-origin: center;
    vector-effect: non-scaling-stroke;
    transition: all 0.1s ease-in-out;
  }
`;

const ValueLabel = styled.text``;

const Wrap = styled.label`
  ${Track} {
    fill: var(--body-text-color---opacity-low);
  }
  ${Handle} {
    fill: var(--link-color---contrast);
  }
  &.is-selected {
    ${Track} {
      fill: var(--link-color);
    }
    ${Handle} {
      fill: var(--link-color---contrast);
    }
  }
  &.is-hovered {
    ${Track} {
      fill: var(--body-text-color---opacity-med);
    }
    ${Handle} {
      fill: var(--link-color---contrast);
    }
  }
  &.is-selected.is-hovered {
    ${Track} {
      fill: var(--link-color---opacity-high);
    }
    ${Handle} {
      fill: var(--link-color---contrast);
    }
  }
  &.is-pressed {
    ${Track},
    ${Handle} {
      transition-duration: 0s;
    }
      
    ${Track} {
      fill: var(--link-color);
    }
    ${Handle} {
      fill: var(--link-color---contrast);
    }
  }
`;

interface ToggleProps extends AriaSwitchProps, HoverProps {
  toggleShape?: { width: number, height: number, inset: number };
  children?: React.ReactNode;
  defaultValue?: boolean;
  checkedLabel?: string;
  unCheckedLabel?: string;
  style: React.CSSProperties;
  onChange?: (value: boolean) => void;
}

export const Toggle = (props: ToggleProps) => {
  const {
    children,
    toggleShape,
    checkedLabel,
    unCheckedLabel,
    style
  } = props;

  let state = useToggleState(props);
  let ref = React.useRef(null);
  let { inputProps } = useSwitch(props, state, ref);
  let { hoverProps, isHovered } = useHover(props);
  let { pressProps, isPressed } = usePress(props);
  let { isFocusVisible, focusProps } = useFocusRing();
  let { visuallyHiddenProps } = useVisuallyHidden();

  return (
    <Wrap
      {...mergeProps(hoverProps, pressProps)}
      {...style}
      className={classnames(
        isHovered && 'is-hovered',
        isFocusVisible && 'is-focused',
        isPressed && 'is-pressed',
        state.isSelected && 'is-selected'
      )}>
      <input
        {...inputProps}
        {...focusProps}
        {...visuallyHiddenProps}
        ref={ref} />
      <Svg

        viewBox={`0 0 ${toggleShape.width} ${toggleShape.height}`}
      >
        <Track
          width={toggleShape.width}
          height={toggleShape.height}
          rx={Math.min(toggleShape.width, toggleShape.height) / 2}
        />
        <FocusRing
          width={`calc(100% + 4px`}
          height={`calc(100% + 4px`}
          x="-2px"
          y="-2px"
          fill="none"
          rx={(Math.min(toggleShape.width, toggleShape.height) / 2) + 2}
          stroke="var(--link-color)"
          opacity={isFocusVisible ? 1 : 0}
          strokeWidth="2px"
        />
        {checkedLabel && <ValueLabel
          x={(toggleShape.width - toggleShape.height) / 2}
          y={toggleShape.height / 2}
          textAnchor="middle"
          dominantBaseline="central"
          fill="var(--link-color---contrast)"
          opacity={state.isSelected ? 1 : 0}
        >
          {checkedLabel}
        </ValueLabel>}
        {unCheckedLabel && <ValueLabel
          x={toggleShape.width - (toggleShape.height / 2)}
          y={toggleShape.height / 2}
          textAnchor="end"
          dominantBaseline="central"
          fill="var(--body-text-color)"
          opacity={state.isSelected ? 0 : 1}
        >{unCheckedLabel}</ValueLabel>}
        <Handle
          x={toggleShape.inset}
          y={toggleShape.inset}
          width={toggleShape.height - (toggleShape.inset * 2)}
          height={toggleShape.height - (toggleShape.inset * 2)}
          rx={(Math.min(toggleShape.width, toggleShape.height) - (toggleShape.inset * 2)) / 2}
          ry={(Math.min(toggleShape.width, toggleShape.height) - (toggleShape.inset * 2)) / 2}
          transform={state.isSelected
            ? `translate(${props.toggleShape.width - props.toggleShape.height} 0)`
            : undefined}
        />
      </Svg>
      {children}
    </Wrap>
  )
}

Toggle.defaultProps = {
  toggleShape: {
    width: 36,
    height: 24,
    inset: 1.5
  }
}

Toggle.Wrap = Wrap;
Toggle.Svg = Svg;
Toggle.Handle = Handle;
Toggle.Track = Track;
Toggle.ValueLabel = ValueLabel;
