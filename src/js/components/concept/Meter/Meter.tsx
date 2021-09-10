import React from 'react';
import styled from 'styled-components';
import { useMeter } from '@react-aria/meter'

import { classnames } from '../../../utils/classnames'

export const MeterWrap = styled.div`
  display: inline-flex;
  flex-direction: column;
  width: 200px;
`;

export const Bar = styled.div`
  height: 0.5rem;
  width: 100%;
  background: var(--body-text-color---opacity-low);
  border-radius: 100em;
  overflow: hidden;
`;

export const Fill = styled.div`
  height: inherit;
  background: var(--link-color);

  .should-change-smoothly & {
    transition: width 0.1s ease;
  }
`;

const Label = styled.span`
  .is-label-hidden & {
    visibility: hidden;
    position: absolute;
  }
`;

const ValueLabel = styled.span``;

const Header = styled.div`
  display: flex;
  justify-content: space-between;
`;

interface MeterProps {
  value: number
  minValue?: number
  maxValue?: number
  label: React.ReactNode
  showLabel?: boolean
  showValueLabel?: boolean
  shouldChangeSmoothly?: boolean
  meterStyle?: React.CSSProperties
}

export const Meter = ({
  label,
  showLabel = !!label,
  showValueLabel = !!label,
  value,
  minValue = 0,
  maxValue = 100,
  meterStyle,
  shouldChangeSmoothly = true,
}: MeterProps) => {

  let { meterProps, labelProps } = useMeter({ label, showValueLabel, value, minValue, maxValue });

  let percentage = (value - minValue) / (maxValue - minValue);
  let barWidth = `${Math.round(percentage * 100)}%`;

  return (
    <MeterWrap {...meterProps} style={meterStyle} className={classnames(
      shouldChangeSmoothly && 'should-change-smoothly',
      showLabel ? '' : 'is-label-hidden',
    )}>
      <Header>
        {label && <Label {...labelProps}>{label}</Label>}
        {showValueLabel && <ValueLabel>{meterProps['aria-valuetext']}</ValueLabel>}
      </Header>
      <Bar>
        <Fill style={{ width: barWidth }} />
      </Bar>
    </MeterWrap>
  );
}