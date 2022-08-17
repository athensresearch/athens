import React from 'react';
import { Box } from '@chakra-ui/react';

const MIN_VW = 20;
const MAX_VW = 80;


const clamp = (value: number, min: number, max: number) => Math.max(Math.min(value, max), min);
const getVW = (e, window) => {
  const innerWidth = window.innerWidth;
  const clientX = e.clientX;
  const calcVW = (innerWidth - clientX) / innerWidth * 100;
  return calcVW;
}

export const RightSidebarResizeControl = (props) => {
  const { onResizeSidebar, isSidebarOpen, sidebarWidth, ...rest } = props;
  const [isDragging, setIsDragging] = React.useState(false);

  const moveHandler = (e) => {
    if (isDragging) {
      e.preventDefault();
      const calcVW = getVW(e, window);
      const clampVW = clamp(calcVW, MIN_VW, MAX_VW);
      onResizeSidebar(clampVW);
    }
  }

  // todo: set graph property block to this value on mouse up
  const mouseUpHandler = (e) => {
    setIsDragging(false);
  }

  React.useEffect(() => {
    window.addEventListener('mousemove', moveHandler);
    window.addEventListener('mouseup', mouseUpHandler);
    return () => {
      window.removeEventListener('mousemove', moveHandler);
      window.removeEventListener('mouseup', mouseUpHandler);
    }
  });

  if (!isSidebarOpen) {
    return null;
  }

  return (
    <Box
      as="button"
      width="3px"
      transform="translateX(50%)"
      position="fixed"
      zIndex={100}
      opacity={0}
      right={sidebarWidth + "vw"}
      height="100%"
      cursor="col-resize"
      onMouseDown={() => setIsDragging(true)}
      onMouseMove={moveHandler}
      onMouseUp={mouseUpHandler}
      bg="link"
      transition="opacity 0.2s ease-in-out"
      _hover={{ opacity: 1 }}
      {...isDragging && { opacity: 1 }}
    >
    </Box>
  );
}