function dump_image(plotnum)

filename=sprintf('mpout/is_demo%d.mp',plotnum);
__gnuplot_raw__('set size 0.7\n');
__gnuplot_raw__('set term mp color solid\n')
__gnuplot_raw__(['set output "',filename,'"\n'])
__gnuplot_raw__('replot\n')
__gnuplot_raw__('set term x11\n')
__gnuplot_raw__('set output\n')

