/**
 * Traducao pt-BR para os componentes do PrimeVue (DatePicker, DataTable).
 * O PrimeVue so embarca ingles; sem isso o calendario abre com
 * "January/Sun/Mon" e o paginador com "Showing 1 to 5 of 10".
 */
export const ptBrLocale = {
  dayNames: ['Domingo', 'Segunda', 'Terça', 'Quarta', 'Quinta', 'Sexta', 'Sábado'],
  dayNamesShort: ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'],
  dayNamesMin: ['D', 'S', 'T', 'Q', 'Q', 'S', 'S'],
  monthNames: [
    'Janeiro',
    'Fevereiro',
    'Março',
    'Abril',
    'Maio',
    'Junho',
    'Julho',
    'Agosto',
    'Setembro',
    'Outubro',
    'Novembro',
    'Dezembro',
  ],
  monthNamesShort: ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'],
  today: 'Hoje',
  clear: 'Limpar',
  dateFormat: 'dd/mm/yy',
  firstDayOfWeek: 0,
  weekHeader: 'Sem',
  emptyMessage: 'Nenhum resultado encontrado',
  emptyFilterMessage: 'Nenhum resultado encontrado',

  // Rotulos de navegacao do DatePicker e do paginador do DataTable. Ficam
  // so em aria-label/title, entao passam despercebidos numa conferida
  // visual — mas sao exatamente o que um leitor de tela anuncia.
  chooseDate: 'Escolher data',
  chooseMonth: 'Escolher mês',
  chooseYear: 'Escolher ano',
  prevMonth: 'Mês anterior',
  nextMonth: 'Próximo mês',
  prevYear: 'Ano anterior',
  nextYear: 'Próximo ano',
  prevDecade: 'Década anterior',
  nextDecade: 'Próxima década',
  firstPageLabel: 'Primeira página',
  lastPageLabel: 'Última página',
  nextPageLabel: 'Próxima página',
  prevPageLabel: 'Página anterior',
  rowsPerPageLabel: 'Registros por página',
  jumpToPageDropdownLabel: 'Ir para a página',
  jumpToPageInputLabel: 'Ir para a página',
}
