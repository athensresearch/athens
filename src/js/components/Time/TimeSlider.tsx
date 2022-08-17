import React from 'react'
import {
  RangeSlider,
  RangeSliderTrack,
  RangeSliderFilledTrack,
  RangeSliderThumb,
  Tooltip
} from '@chakra-ui/react'


function format (v) {
  return new Date(v).toUTCString();
}


export const TimeSlider = (props) => {
  const {min, max, onChange} = props;
  const [sliderValue, setSliderValue] = React.useState([min, max])
  const [showTooltip, setShowTooltip] = React.useState(false)

  return (
  <RangeSlider
    aria-label={['min', 'max']}
    defaultValue={[min, max]}
    min={min}
    max={max}
    onChange={(v) => {setSliderValue(v); onChange(v);}}
    onMouseEnter={() => setShowTooltip(true)}
    onMouseLeave={() => setShowTooltip(false)} >
      <RangeSliderTrack>
      <RangeSliderFilledTrack />
      </RangeSliderTrack>
      <Tooltip
        hasArrow
        bg='teal.500'
        color='white'
        placement='top'
        isOpen={showTooltip}
        label={`${format(sliderValue[0])}`} >
      <RangeSliderThumb index={0} />
      </Tooltip>
      <Tooltip
        hasArrow
        bg='teal.500'
        color='white'
        placement='top'
        isOpen={showTooltip}
        label={`${format(sliderValue[1])}`} >
      <RangeSliderThumb index={1} />
      </Tooltip>
    </RangeSlider>
  )
}
