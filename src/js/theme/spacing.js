
const scale = [ 0.25, 0.5, 1, 1.5, 2, 2.5, 3, 3.5, 4, 5, 6, 7, 8, 9, 10, 12, 14, 16, 20, 24, 28 ];

const cssSize = (n) => `${n / 2}rem`;

const makeSpace = (initial) => {
  const space = initial;

  scale.forEach(scale => {
    space[ `${scale}` ] = cssSize(scale);
  })
  return space;
}

export const spacing = {
  space: makeSpace({
    px: '1px'
  })
}
